package org.softlang.ura.content

import org.softlang.ura.content.Mime
import org.softlang.ura.content.mime
import org.softlang.ura.util.notNull
import kotlin.reflect.KClass

/**
 * Extended MIME type with runtime type information.
 * @param T The runtime type's static type
 * @param mime The MIME type
 * @param type The runtime type's reflected type
 */
data class XMime<T : Any>(val mime: Mime, val type: KClass<T>)

/**
 * Creates an extended MIME type inferring [T] from the context.
 * @param T The runtime type's static type
 * @param mime The MIME type
 * @return Returns an extended MIME type
 */
inline fun <reified T : Any> xMime(mime: Mime) =
        XMime(mime, T::class)

/**
 * Creates an extended MIME type inferring [T] from the context. Parses the MIME type from a stirng.
 * @param T The runtime type's static type
 * @param mimeString The MIME represented as a string
 * @return Returns an extended MIME type or null if string is not parsable.
 */
inline fun <reified T : Any> xMime(mimeString: String) =
        mime(mimeString) notNull { XMime(it, T::class) }