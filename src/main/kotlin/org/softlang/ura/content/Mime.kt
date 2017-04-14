package org.softlang.ura.content

import org.softlang.ura.util.nc
import org.softlang.ura.util.notNull
import org.softlang.ura.util.opt

/**
 * A fully represented MIME type.
 * @param top The Top level type
 * @param tree The Tree designator
 * @param sub The subtype
 * @param suffix The type suffix
 * @param params The parameters
 */
data class Mime(val top: String, val tree: String?, val sub: String, val suffix: String?, val params: String?) {
    /**
     * Map of the parameters, interpreted as key-value assignments
     */
    val paramsMap by lazy {
        params?.trim()
                ?.split(' ')
                ?.associate { val (l, r) = it.trim().split('='); l to r }
                ?: emptyMap()
    }

    override fun toString() =
            "$top/${(tree nc ".") ?: ""}$sub${("+" nc suffix) ?: ""}${("; " nc params) ?: ""}"
}

/**
 * Parsing regular expression for fully represented MIME types.
 */
private val MIME_REGEX = Regex("""([^/]+)/(?:([^.]+)\.)?([^+;]+)(?:\+([^;]+))?(?:;(.+))?""")

/**
 * Parsing regular expression for the subtype in MIME types.
 */
private val MIME_SUBTYPE_REGEX = Regex("""(?:([^.]+)\.)?([^+;]+)(?:\+([^;]+))?""")

/**
 * Constructs a MIME type.
 */
fun mime(top: String, tree: String? = null, sub: String, suffix: String? = null, params: String? = null) =
        Mime(top, tree, sub, suffix, params)

/**
 * Constructs a MIME type, tree, sub type and suffix are in [eSub].
 */
fun mime(top: String, eSub: String, params: String? = null) =
        MIME_SUBTYPE_REGEX.matchEntire(eSub) notNull {
            val (tree, sub, suffix) = it.destructured

            // Trim matches and convert to optional where appropriate
            Mime(top, tree.trim().opt, sub.trim(), suffix.trim().opt, params)
        }

/**
 * Constructs a MIME type, tree, sub type and suffix are in [eSub].
 */
fun mime(top: String, eSub: String, params: Map<String, String>) =
        MIME_SUBTYPE_REGEX.matchEntire(eSub) notNull {
            val (tree, sub, suffix) = it.destructured

            // Join the provided key values
            val joint = params.entries.joinToString { "${it.key}=${it.value}" }

            // Trim matches and convert to optional where appropriate
            Mime(top, tree.trim().opt, sub.trim(), suffix.trim().opt, joint.opt)
        }

/**
 * Constructs a MIME type, tree, sub type and suffix are in [eSub].
 */
fun mime(top: String, eSub: String, vararg pairs: Pair<String, String>) =
        MIME_SUBTYPE_REGEX.matchEntire(eSub) notNull {
            val (tree, sub, suffix) = it.destructured

            // Join the provided key values
            val joint = pairs.joinToString { "${it.first}=${it.second}" }

            // Trim matches and convert to optional where appropriate
            Mime(top, tree.trim().opt, sub.trim(), suffix.trim().opt, joint.opt)
        }

/**
 * Parses a MIME type.
 *
 * @param string The string to parse
 * @return Returns the MIME type or null if failed
 */
fun mime(string: CharSequence) =
        MIME_REGEX.matchEntire(string) notNull {
            val (top, tree, sub, suffix, params) = it.destructured

            // Trim matches and convert to optional where appropriate
            Mime(top.trim(), tree.trim().opt, sub.trim(), suffix.trim().opt, params.trim().opt)
        }


fun main(args: Array<String>) {
    println(mime("text/html+xml; charset=UTF-8"))
    println(mime("application", "xml", "charset" to "UTF-8"))
    println(mime("application", "xml", mapOf()))
}