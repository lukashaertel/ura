package org.softlang.ura.util

/**
 * Interprets the value as a constant function.
 * @param T The type of the value
 * @receiver The value to use as constant value
 * @return Returns a provider, or, 0-ary function.
 */
val <T> T.const get() = { this }

/**
 * Introduces a new, unused parameter.
 * @param T The type of the parameter to introduce
 * @param U The return type of the function to unbind
 * @receiver The function to unbind
 * @return Returns a function with a new unused parameter.
 */
inline fun <reified T, reified U> (() -> U).unbind() =
        { _: T -> this() }

/**
 * Binds the parameter of the function.
 * @param T The type of the parameter
 * @param U The return type of the function to bind
 * @receiver The function to bind
 * @return Returns a function with the first parameter bound
 */
inline fun <reified T, reified U> ((T) -> U).bind(t: T) =
        { this(t) }

/**
 * [unbind] for a new left parameter of a binary function, from a unary function.
 */
inline fun <reified T, reified U, reified V> ((T) -> U).unbindLeft() =
        { _: V, t: T -> this(t) }

/**
 * [unbind] for a new right parameter of a binary function, from a unary function.
 */
inline fun <reified T, reified U, reified V> ((T) -> U).unbindRight() =
        { t: T, _: V -> this(t) }

/**
 * [bind] for the left parameter of a binary function.
 */
inline fun <reified T, reified U, reified V> ((T, U) -> V).bindLeft(t: T) =
        { u: U -> this(t, u) }

/**
 * [bind] for the right parameter of a binary function.
 */
inline fun <reified T, reified U, reified V> ((T, U) -> V).bindRight(u: U) =
        { t: T -> this(t, u) }

/**
 * Returns a new identity function.
 * @param T The domain and codomain of the identity function
 * @return Returns a new function.
 */
inline fun <reified T> id() =
        { t: T -> t }