package org.softlang.ura

import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.nio.charset.Charset

/**
 * This file is the concrete definition of a URA resolution.
 */

/**
 * A URA  node.
 */
typealias URA = Nav<String, URI>

/**
 * Root URA
 */
fun ura(uri: URI, then: URA? = null) = URA(uri.scheme, uri, then)

/**
 * Continuation URA
 */
fun ura(operator: String, argument: URI, then: URA? = null) = URA(operator, argument, then)


/**
 * Root URA
 */
fun ura(uri: String, then: URA? = null) = ura(URI(uri), then)

/**
 * Continuation URA
 */
fun ura(operator: String, argument: String, then: URA? = null) = ura(operator, URI(argument), then)

/**
 * Full URA from root argument and consecutive pairs of operator and URI.
 */
fun uraFrom(vararg string: String): URA {
    // Check conditions on the input
    if (string.isEmpty())
        throw IllegalArgumentException("No argument supplied")
    if ((string.size - 1) % 2 != 0)
        throw IllegalArgumentException("Requires 1, 3, 5 ... arguments.")

    // Recursive function to evaluate the segments
    fun f(i: Int): URA? =
            if (i + 1 < string.size)
                ura(string[i], string[i + 1], f(i + 2))
            else
                null

    // Return initial item and continue with paired segments
    return ura(string[0], f(1))
}

// TODO Lookup operations, also maybe we need runtime type here
typealias Bundle = Map<Mime, Any?>

interface Problem

/**
 * A URA resolver.
 */
typealias URAResolver = Resolver<URI, Bundle, Problem>

/**
 * A URA context.
 */
typealias URAContext = Context<String, URI, Bundle, Problem>

fun main(args: Array<String>) {
    data class GenericProblem(val message: String) : Problem

    class HTTPResolver : URAResolver {
        override fun resolve(context: Bundle, argument: URI): Choice<Bundle, Problem> {
            // Get a connection to the
            val x = argument.toURL().openConnection() as? HttpURLConnection
                    ?: return right(GenericProblem("Cannot open HTTP connection"))

            return mime(x.contentType).orUnit mapLeft {
                val i = x.inputStream
                        .reader(it.paramsMap["charset"] ?: "UTF-8")
                        .use(InputStreamReader::readText)

                mapOf(it to i)
            } mapRight {
                GenericProblem("Unable to parse MIME type of the response")
            }
        }
    }

    class SelectResolver : URAResolver {
        override fun resolve(context: Bundle, argument: URI): Choice<Bundle, Problem> {
            val e = context.entries.first { it.key.top == "text" && it.key.sub == "html" }
            return left(mapOf(e.key to (e.value as String).substring(10..20))) // TODO This is just a thing
        }

    }

    val ctx = object : URAContext(emptyMap()) {
        override fun operation(context: Bundle, o: String): Choice<Resolver<URI, Bundle, Problem>, Problem> {
            // TODO: Formalize selection
            if (context.isEmpty()) {
                when (o) {
                    "https", "http" -> return left(HTTPResolver())
                }
            }

            if (context.keys.any { it.top == "text" && it.sub == "html" }) {
                when (o) {
                    "select" -> return left(SelectResolver())
                }
            }

            return right(GenericProblem("No resolver found"))
        }
    }

    val x = ctx.resolve(uraFrom("http://www.google.de"))
    val y = ctx.resolve(uraFrom("http://www.google.de", "select", "arg://line"))
    println(x)
    println(y)

}