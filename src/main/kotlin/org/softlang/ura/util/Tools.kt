package org.softlang.ura.util

import java.io.InputStream
import java.nio.charset.Charset

/**
 * Applies [then] if receiver is not *null*, otherwise returns *null* itself.
 *
 * @param T The argument of the transformation
 * @param U The result of the transformation
 * @receiver The nullable argument
 * @param then The transformation
 * @return Returns *null* if argument is *null*, otherwise applies [then]
 */
inline infix fun <reified T, reified U> T?.notNull(then: (T) -> U) =
        if (this != null) then(this) else null

/**
 * Compares two objects that might be *null*, preferring non-*null* objects.
 * @param T The type of the objects
 * @receiver The first object
 * @param other The second object
 * @return Returns *1* if receiver is *null*, *-1* if [other] is *null* and the result of [Comparable.compareTo] on
 * the receiver with [other] otherwise.
 */
inline infix fun <reified T : Comparable<T>> T?.compareNullable(other: T?) = when {
    this == null -> 1
    other == null -> -1
    else -> this.compareTo(other)
}

/**
 * Returns true if the receiver is represented as the string through [toString]
 */
inline infix fun <reified T> T?.repEq(other: String) =
        if (this != null)
            toString() == other
        else
            "null" == other

/**
 * Nullable concatenation, returns the concatenation only iff *both* strings are not *null*, otherwise *null*.
 * @receiver The first string
 * @param other The second string
 * @return Returns the concatenation
 */
infix fun String?.nc(other: String?) =
        if (this == null || other == null) null else this + other

/**
 * Converts a string that represents *null*s by being empty to an actual *null*.
 * @receiver The string to convert
 */
val String.opt get() = if (isEmpty()) null else this

@Suppress("NOTHING_TO_INLINE")
inline fun InputStream.reader(charset: String) = reader(Charset.forName(charset))

/**
 * Asserts that the receiver is equal to [other], returns the receiver.
 */
inline infix fun <reified T> T.shouldBe(other: T) =
        apply { if (this != other) error("$this should be $other") }

/**
 * Asserts that the receiver matches [p], returns the receiver.
 */
inline infix fun <reified T> T.shouldMatch(p: T.() -> Boolean) =
        apply { if (!this.p()) error("$this does not match the predicate") }