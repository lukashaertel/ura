package org.softlang.ura

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

/**
 * A URA resolver.
 */
typealias URAResolver = Resolver<URI, Set<Mime>, Map<Mime, Any?>>

/**
 * A URA context.
 */
typealias URAContext = Context<String, URI, Set<Mime>, Map<Mime, Any?>>

/**
 * A URA default context
 */
typealias URADefaultContext = DefaultContext<String, URI, Set<Mime>, Map<Mime, Any?>>

/**
 * Creates a URA default context with the standard *type* and *instance*.
 * @param resolvers The resolvers as a map of operation to URA resolvers.
 * @return Returns a new URA default context.
 */
fun uraDefaultContext(resolvers: Map<String, URAResolver>) =
        URADefaultContext(emptySet(), emptyMap(), resolvers)

/**
 * Creates a URA default context with the standard *type* and *instance*.
 * @param pairs The list of entries in the resolvers map
 * @return Returns a new URA default context.
 */
fun uraDefaultContext(vararg pairs: Pair<String, URAResolver>) =
        uraDefaultContext(mapOf(*pairs))

fun main(args: Array<String>) {
    data class GenericProblem(val message: String) : Problem

    class HTTPResolver : URAResolver {
        override fun type(argument: URI, type: Set<Mime>): Choice<Set<Mime>, Problem> {
            // Get a connection to the
            val x = argument.toURL().openConnection() as? HttpURLConnection
                    ?: return right(GenericProblem("Cannot open HTTP connection"))

            return mime(x.contentType).orUnit mapLeft {
                setOf(it)
            } mapRight {
                GenericProblem("Unable to parse MIME type of the response")
            }
        }

        override fun instance(argument: URI, type: Set<Mime>, instance: Map<Mime, Any?>): Map<Mime, Any?> {
            val x = argument.toURL().openConnection() as HttpURLConnection

            val m = mime(x.contentType)!!
            val c = Charset.forName(m.paramsMap["charset"] ?: "UTF-8")
            return mapOf(m to x.inputStream.reader(c).use { it.readText() })
        }
    }

    val ctx = uraDefaultContext(
            "http" to HTTPResolver(),
            "https" to HTTPResolver()
    )

    val x = ctx.resolve(uraFrom("http://www.google.de"))
    val y = ctx.resolve(uraFrom("http://www.google.de", "select", "arg://line"))
    println(x)
    println(y)

}