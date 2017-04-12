package org.softlang.ura

import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset

/**
 * Applies [then] if receiver is not *null*, otherwise returns *null* itself.
 *
 * @param T The argument of the transformation
 * @param U The result of the transformation
 * @receiver The nullable argument
 * @param then The transformation
 * @return Returns null if argument is *null*, otherwise applies [then]
 */
inline infix fun <reified T, reified U> T?.notNull(then: (T) -> U) =
        if (this != null) then(this) else null

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