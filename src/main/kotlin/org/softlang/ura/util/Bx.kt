package org.softlang.ura.util

/**
 * A binary transformation from [T] to [U] and [U] to [T].
 * @param T The first domain
 * @param U The second domain
 */
interface Bx<T, U> {
    /**
     * Applies the binary transformation from [T] to [U].
     */
    operator fun invoke(t: T): U

    /**
     * Applies the binary transformation from [U] to [T].
     */
    fun invokeReverse(u: U): T

    /**
     * The reverse of the binary transformation, where [T] and [U] are switched.
     */
    val reverse: Bx<U, T> get() = object : Bx<U, T> {
        override fun invoke(t: U) = this@Bx.invokeReverse(t)

        override fun invokeReverse(u: T) = this@Bx.invoke(u)
    }
}

/**
 * Creates a binary transformation from both [forward]: [T] to [U] and [reverse]: [U] to [T].
 */
inline fun <T, U> bx(crossinline forward: (T) -> U, crossinline reverse: (U) -> T) = object : Bx<T, U> {
    override fun invoke(t: T) =
            forward(t)

    override fun invokeReverse(u: U) =
            reverse(u)
}

/**
 * Creates a binary transformation from both receiver: [T] to [U] and [reverse]: [U] to [T]. Note: since Kotlin does
 * not yet support crossinline on receivers, it might be preferable to use [bx].
 */
inline infix fun <T, U> ((T) -> U).withReverse(crossinline reverse: (U) -> T) = object : Bx<T, U> {
    override fun invoke(t: T) =
            this@withReverse(t)

    override fun invokeReverse(u: U) =
            reverse(u)
}