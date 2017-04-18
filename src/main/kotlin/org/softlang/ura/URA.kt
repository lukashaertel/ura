package org.softlang.ura

import org.softlang.ura.content.*
import org.softlang.ura.util.*
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.nio.charset.Charset
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

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

interface Problem

/**
 * A URA resolver.
 */
typealias URAResolver = Resolver<URI, Content, Problem>

/**
 * A URA context.
 */
typealias URAContext = Context<String, URI, Content, Problem>


fun main(args: Array<String>) {
    data class GenericProblem(val message: String) : Problem

    class HTTPResolver : URAResolver {
        override fun resolve(context: Content, argument: URI): Choice<Content, Problem> {
            // Get a connection to the
            val x = argument.toURL().openConnection() as? HttpURLConnection
                    ?: return right(GenericProblem("Cannot open HTTP connection"))

            return mime(x.contentType).orUnit mapLeft {
                content {
                    it by {
                        x.inputStream
                                .reader(it.paramsMap["charset"] ?: "UTF-8")
                                .use(InputStreamReader::readText)
                    }
                }
            } mapRight {
                GenericProblem("Unable to parse MIME type of the response")
            }
        }
    }

    class SelectResolver : URAResolver {
        override fun resolve(context: Content, argument: URI): Choice<Content, Problem> {
            return context.with("text/html") { s: String ->
                content {
                    "text/html" by {
                        s.substring(10..20)
                    }
                }
            }.resolved.orUnit.mapRight {
                GenericProblem("Cannot handle the context")
            }
        }

    }

    val ctx = object : URAContext(Content.empty) {
        override fun operation(context: Content, op: String): Choice<Resolver<URI, Content, Problem>, Problem> {
            // TODO: Formalize selection
            if (context.isEmpty()) {
                when (op) {
                    "https", "http" -> return left(HTTPResolver())
                }
            }

            if (xMime<String>("text/html") in context) {
                when (op) {
                    "select" -> return left(SelectResolver())
                }
            }

            return right(GenericProblem("No resolver found"))
        }
    }

    val x = ctx.resolve(uraFrom("http://www.google.de"))
    val y = ctx.resolve(uraFrom("http://www.google.de", "select", "arg://line"))
    println(x mapLeft { it.materialize() })
    println(y mapLeft { it.materialize() })

}