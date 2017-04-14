package org.softlang.ura.content

import com.google.common.collect.ImmutableMap
import org.softlang.ura.util.*
import java.util.spi.CalendarDataProvider
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.isSuperclassOf


/**
 * Content of extended MIME types instantiated by provider methods. This class needs to be created by [content] to
 * ensure type safe associations.
 *
 * @property associated Private list of untyped associations.
 */
data class Content(private val associated: Map<XMime<*>, () -> Any>) {
    companion object {
        /**
         * Compares pairs of items with class information as first by their generality.
         */
        private val generalityComparator = Comparator<Pair<KClass<*>, *>> { (k1, _), (k2, _) ->
            when {
                k1 == k2 -> 0
                k1.isSubclassOf(k2) -> -1
                k2.isSubclassOf(k1) -> 1
                else -> k1.qualifiedName compareNullable k2.qualifiedName
            }
        }
    }

    /**
     * Inner class wrapping the result of a [with] call for continuation.
     * @property resolved The actual value
     */
    inner class Item<U : Any>(val response: XMime<*>, val resolved: U?) {
        fun <T : Any> with(xMime: XMime<T>, block: (T) -> U) =
                if (resolved != null)
                    this
                else
                    this@Content.with(xMime, block)

        inline fun <reified T : Any> with(mime: Mime, noinline block: (T) -> U): Item<U> {
            return with(xMime<T>(mime), block)
        }

        inline fun <reified T : Any> with(string: String, noinline block: (T) -> U): Item<U> {
            return with(xMime<T>(string) ?: return Item(xMimeNothing, null), block)
        }
    }

    /**
     * Groups the associations and sorts the group by generality.
     */
    val coAssociated by lazy {
        associated.entries.groupBy({ (k, _) -> k.mime }) { (k, v) ->
            k.type to v
        }.mapValues {
            it.value.sortedWith(generalityComparator)
        }
    }

    /**
     * Lifts type information from the associations.
     */
    val deAssociated by lazy {
        associated.entries.associateAll {
            it.key.mime to it.value
        }
    }

    fun <T : Any, U : Any> with(xMime: XMime<T>, block: (T) -> U): Item<U> {
        // Test for direct hit
        val x = associated[xMime]
        if (x != null) {
            @Suppress("UNCHECKED_CAST")
            val t = x() as T
            return Item(xMime, block(t))
        }

        // Test for indirect hit
        val ys = coAssociated[xMime.mime].orEmpty()
        val y = ys.firstOrNull { (k, _) -> xMime.type.isSuperclassOf(k) }
        if (y != null) {
            @Suppress("UNCHECKED_CAST")
            val t = y.second() as T
            return Item(XMime(xMime.mime, y.first), block(t))
        }

        // Not present
        return Item(xMimeNothing, null)
    }

    inline fun <reified T : Any, U : Any> with(mime: Mime, noinline block: (T) -> U): Item<U> {
        return with(xMime<T>(mime), block)
    }

    inline fun <reified T : Any, U : Any> with(string: String, noinline block: (T) -> U): Item<U> {
        return with(xMime<T>(string) ?: return Item(xMimeNothing, null), block)
    }

}

/**
 * Interface for content building, used by [content].
 */
abstract class ContentPut {
    /**
     * Puts the extended MIME type and the associated provider.
     * @param T The type of the values
     * @receiver The extended MIME type
     * @param provider The provider for values of the given type
     */
    abstract infix fun <T : Any> XMime<T>.by(provider: () -> T)

    /**
     * Wraps [by], inferring [T] from the provider's result type.
     * @param T The type of the extended MIME
     * @receiver The simple MIME type
     * @param provider The provider method
     */
    inline infix fun <reified T : Any> Mime.by(noinline provider: () -> T) {
        XMime(this, T::class) by provider
    }

    /**
     * Wraps [by], inferring [T] from the provider's result type.
     * @param T The type of the extended MIME
     * @receiver The simple MIME type as a string, if not parsable, an error is thrown
     * @param provider The provider method
     */
    inline infix fun <reified T : Any> String.by(noinline provider: () -> T) {
        XMime(mime(this) ?: error("Cannot parse receiver as mime"), T::class) by provider
    }
}

/**
 * Crates the content from the given provider associations.
 */
inline fun content(block: ContentPut.() -> Unit): Content {
    // Make an immutable map builder
    val b = ImmutableMap.builder <XMime<*>, () -> Any>()

    // Content put wraps the put method in a type safe fashion
    object : ContentPut() {
        override fun <T : Any> XMime<T>.by(provider: () -> T) {
            b.put(this, provider)
        }
    }.block()

    // Return the resulting mappings
    return Content(b.build())
}

fun main(args: Array<String>) {
    open class X(val i: Int)
    class Y(i: Int, val j: Float) : X(i)
    class Z(i: Int, val j: String) : X(i)

    /**
     * Example of resolution of inheritance
     */

    val x = content {
        "app/foo" by { X(0) }
        "app/foo" by { Y(1, 1.0f) }
        "app/foo" by { Z(2, "Hello") }
    }

    // .. Raw MIME type
    // .... vvvvvvv
    x.with("app/foo") { x: X ->
        x.i shouldBe 0
    }

    val y = content {
        "app/foo" by { Y(1, 1.0f) }
        "app/foo" by { Z(2, "Hello") }
    }

    // ............. Runtime type
    // ................... v
    y.with("app/foo") { x: X ->
        x.i shouldBe 1
    }


    /**
     * Example of multi type handling
     */

    val z = content {
        "app/int" by { 100 }
        "app/string" by { "23400" }
    }

    // First block determines the result of the sequence
    val r1 = z.with("app/string") { s: String ->
        s.toInt() + 1
    }.with("app/int") { i: Int ->
        i + 1
    }

    r1.response shouldMatch { mime repEq "app/string" }
    r1.resolved shouldBe 23401

    val r2 = z.with("app/float") { s: String ->
        s.toInt() * 20
    }.with("app/int") { i: Int ->
        i * 20
    }

    r2.response shouldMatch { mime repEq "app/int" }
    r2.resolved shouldBe 2000

}