package org.softlang.ura

/**
 * A binary transformation from [T] to [U] and [U] to [T].
 * @param T The first domain
 * @param U The second domain
 */
interface BX<T, U> {
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
    val reverse: BX<U, T> get() = object : BX<U, T> {
        override fun invoke(t: U) = this@BX.invokeReverse(t)

        override fun invokeReverse(u: T) = this@BX.invoke(u)
    }
}

/**
 * Creates a binary transformation from both receiver: [T] to [U] and [reverse]: [U] to [T].
 */
inline infix fun <T, U> ((T) -> U).withReverse(crossinline reverse: (U) -> T) = object : BX<T, U> {
    override fun invoke(t: T) =
            this@withReverse(t)

    override fun invokeReverse(u: U) =
            reverse(u)

}