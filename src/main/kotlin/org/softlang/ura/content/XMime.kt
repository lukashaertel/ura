package org.softlang.ura.content

import org.softlang.ura.content.Mime
import org.softlang.ura.content.mime
import org.softlang.ura.util.notNull
import org.softlang.ura.util.equalMapping
import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf

/**
 * Extended MIME type with runtime type information.
 * @param T The runtime type's static type
 * @param mime The MIME type
 * @param type The runtime type's reflected type
 */
data class XMime<T : Any>(val mime: Mime, val type: KClass<T>) {
    /**
     * Returns the extended MIME type without parameters.
     */
    val lifted get() = XMime(mime.lifted, type)


    /**
     * Returns true if the extended MIME types are equal with unification, i.e., lifted representation is data equal and
     * parameter spaces are equalMapping.
     * @param other The other extended MIME type
     * @return True if equalMapping equal
     */
    infix fun unifies(other: XMime<*>) =
            type.isSuperclassOf(other.type) && mime unifies other.mime

    /**
     * Returns the unified extended MIME type.
     * @param other The other extended MIME type
     * @return The unified extended MIME type or null if not equalMapping.
     */
    infix fun unify(other: XMime<*>) =
            if (this unifies other)
                XMime(mime unify other.mime, type)
            else
                throw IllegalArgumentException("$other does not unify with $this.")

}

/**
 * Creates an extended MIME type inferring [T] from the context.
 * @param T The runtime type's static type
 * @param mime The MIME type
 * @return Returns an extended MIME type
 */
inline fun <reified T : Any> xMime(mime: Mime) =
        XMime(mime, T::class)

/**
 * Creates an extended MIME type inferring [T] from the context. Parses the MIME type from a string.
 * @param T The runtime type's static type
 * @param mimeString The MIME represented as a string
 * @return Returns an extended MIME type or null if string is not parsable.
 */
inline fun <reified T : Any> xMime(mimeString: String) =
        mime(mimeString) notNull { XMime(it, T::class) }

/**
 * Value of an extended MIME that represents *nothing*.
 */
val xMimeNothing = XMime(Mime("application", null, "nothing", null, null), Nothing::class)