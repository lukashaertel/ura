package org.softlang.ura

import org.softlang.ura.content.*
import org.softlang.ura.util.*
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI

/**
 * A formally defined provider with a configuration [C] that provides [Content] from a [URI].
 * @param C The configuration type, usually a metamodel
 * @param provide The provide method
 */
data class FormalProvider<in C>(
        val provide: C.(URI) -> Choice<Content, Problem>)

/**
 * A formally defined handler from source to destination XMime, given a configuration [C] and a handler that, in [C],
 * transforms [T] with the [URI] to [U].
 * @param C The configuration type, usually a metamodel
 * @param T The source type
 * @param U The destination type
 * @param src The source extended MIME
 * @param dst The destination extended MIME
 * @param handle The handle method
 */
data class FormalHandler<in C, T : Any, U : Any>(
        val src: XMime<T>,
        val dst: XMime<U>,
        val handle: C.(URI, T) -> Choice<U, Problem>)

/**
 * Configurator block for a resolver, collects formal handlers using [via] and [idVia].
 */
class FormalHandlerCollector<C> {
    /**
     * Mutable set of collected handles.
     */
    val handlers = hashSetOf<FormalHandler<C, *, *>>()

    /**
     * For a pair of MIMEs, adds a formal handler to the collected handlers, inferring [T] and [U] from the context.
     * @param T The source type
     * @param U The destination type
     * @receiver A pair encapsulating source and target MIME type
     * @param handle The handle method
     */
    @kotlin.jvm.JvmName("viaMime")
    inline infix fun <reified T : Any, reified U : Any>
            Pair<Mime, Mime>.via(noinline handle: C.(URI, T) -> Choice<U, Problem>) =
            FormalHandler(xMime<T>(first), xMime<U>(second), handle).apply {
                handlers += this
            }

    /**
     * For a pair of MIME strings, adds a formal handler to the collected handlers, inferring [T] and [U] from the
     * context.
     * @param T The source type
     * @param U The destination type
     * @receiver A pair encapsulating source and target MIME type as strings
     * @param handle The handle method
     */
    @kotlin.jvm.JvmName("viaParsed")
    inline infix fun <reified T : Any, reified U : Any>
            Pair<String, String>.via(noinline handle: C.(URI, T) -> Choice<U, Problem>) =
            FormalHandler(xMime<T>(first) ?: throw IllegalArgumentException("Source MIME format illegal"),
                    xMime<U>(second) ?: throw IllegalArgumentException("Destination MIME format illegal"),
                    handle).apply {
                handlers += this
            }


    /**
     * For a MIME string, adds a formal handler to the collected handlers, inferring [T] from the context.
     * @param T The source and destination type
     * @receiver The MIME type as a string
     * @param handle The handle method
     */
    @kotlin.jvm.JvmName("idViaMime")
    inline infix fun <reified T : Any>
            Mime.idVia(noinline handle: C.(URI, T) -> Choice<T, Problem>) =
            FormalHandler(xMime<T>(this), xMime<T>(this), handle).apply {
                handlers += this
            }

    /**
     * For a MIME, adds a formal handler to the collected handlers, inferring [T] from the context.
     * @param T The source and destination type
     * @receiver The MIME type
     * @param handle The handle method
     */
    @kotlin.jvm.JvmName("idViaParsed")
    inline infix fun <reified T : Any>
            String.idVia(noinline handle: C.(URI, T) -> Choice<T, Problem>) =
            FormalHandler(xMime<T>(this) ?: throw IllegalArgumentException("Source MIME format illegal"),
                    xMime<T>(this) ?: throw IllegalArgumentException("Destination MIME format illegal"),
                    handle).apply {
                handlers += this
            }
}

/**
 * A formal root resolver, bound to a [config] of type [C], resolving operations [ops] using the [provider].
 * @param C The type of the config, usually a metamodel
 * @param config The config or metamodel
 * @param ops The operations this resolver handles
 * @param provider The single provider
 */
class FormalRootResolver<C>(
        val config: C,
        val ops: Set<String>,
        val provider: FormalProvider<C>) : URAResolver {
    override fun resolve(context: Content, argument: URI): Choice<Content, Problem> {
        // Assert root status, just to be sure
        context shouldBe Content.empty

        // Return the provider implementation of configurated provision
        return provider.provide(config, argument)
    }
}

data class FormalNestedResolverUnsatisfied<C>(
        val message: String,
        val formalNestedResolver: FormalNestedResolver<C>,
        val context: Content,
        val argument: URI) : Problem

/**
 * A formal nested resolver, bound to a [config] of type [C], resolving operations [ops] using the [handlers].
 * @param C The type of the config, usually a metamodel
 * @param config The config or metamodel
 * @param ops The operations this resolver handles
 * @param handlers The handlers to apply the resolver
 */
class FormalNestedResolver<C>(
        val config: C,
        val ops: Set<String>,
        val handlers: Set<FormalHandler<C, *, *>>) : URAResolver {
    /**
     * Handlers associated by their source extended MIME type.
     */
    val handlersBySource by lazy { handlers.associateBy(FormalHandler<C, *, *>::src) }

    /**
     * Handlers associated by their target extended MIME type.
     */
    val handlersByTarget by lazy { handlers.associateBy(FormalHandler<C, *, *>::dst) }

    override fun resolve(context: Content, argument: URI): Choice<Content, Problem> {
        if (handlersBySource.none { it.key in context })
            return right(FormalNestedResolverUnsatisfied("Content requirements unsatisfied", this, context, argument))

        return left(Content(handlersBySource
                .values
                .filter { it.src in context }
                .mapNotNull {
                    // The validity of the cast is ensured by the context
                    @Suppress("UNCHECKED_CAST")
                    val arb = it.handle as C.(URI, Any) -> Any

                    // Read from context the given type
                    context.with(it.src) { x: Any ->
                        // Provide the destination extended MIME
                        it.dst to { arb(config, argument, x) }
                    }.resolved
                }.associate { it }))
    }
}

class FormalResolverCollector {
    /**
     * Mutable set of collected root resolvers.
     */
    val rootResolvers = hashSetOf<FormalRootResolver<*>>()

    /**
     * Mutable set of collected nested resolvers.
     */
    val nestedResolvers = hashSetOf<FormalNestedResolver<*>>()

    /**
     * Creates a formal from a provider. The first argument is the configuration or metamodel.
     * @param C The type of the config
     * @receiver The set of operations to handle
     * @param config The config
     * @param provider A provider
     * @return Returns a formal resolver
     */
    fun <C> Set<String>.rootWith(config: C, provider: C.(URI) -> Choice<Content, Problem>) =
            FormalRootResolver(config, this, FormalProvider(provider)).apply {
                rootResolvers += this
            }

    /**
     * Creates a formal from a provider. The first argument is the configuration or metamodel.
     * @param C The type of the config
     * @receiver The operation to handle
     * @param config The config
     * @param provider A provider
     * @return Returns a formal resolver
     */
    fun <C> String.rootWith(config: C, provider: C.(URI) -> Choice<Content, Problem>) =
            FormalRootResolver(config, setOf(this), FormalProvider(provider)).apply {
                rootResolvers += this
            }

    /**
     * Creates a formal from a provider. No config used by the resolution.
     * @receiver The set of operations to handle
     * @param provider A provider
     * @return Returns a formal resolver
     */
    fun Set<String>.rootWith(provider: Unit.(URI) -> Choice<Content, Problem>) =
            FormalRootResolver(Unit, this, FormalProvider(provider)).apply {
                rootResolvers += this
            }

    /**
     * Creates a formal from a provider. No config used by the resolution.
     * @receiver The operation to handle
     * @param provider A provider
     * @return Returns a formal resolver
     */
    fun String.rootWith(provider: Unit.(URI) -> Choice<Content, Problem>) =
            FormalRootResolver(Unit, setOf(this), FormalProvider(provider)).apply {
                rootResolvers += this
            }

    /**
     * Creates a formal resolver by collecting formal handlers. The first argument is the configuration or metamodel.
     * @param C The type of the config
     * @receiver The set of operations to handle
     * @param config The config
     * @param configure A configuration block
     * @return Returns a formal resolver
     */
    inline fun <C> Set<String>.nestedWith(config: C, configure: FormalHandlerCollector<C>.() -> Unit) =
            FormalNestedResolver(config, this, FormalHandlerCollector<C>().apply(configure).handlers.toSet()).apply {
                nestedResolvers += this
            }

    /**
     * Creates a formal resolver by collecting formal handlers. The first argument is the configuration or metamodel.
     * @param C The type of the config
     * @receiver The operation to handle
     * @param config The config
     * @param configure A configuration block
     * @return Returns a formal resolver
     */
    inline fun <C> String.nestedWith(config: C, configure: FormalHandlerCollector<C>.() -> Unit) =
            FormalNestedResolver(config, setOf(this), FormalHandlerCollector<C>().apply(configure).handlers.toSet()).apply {
                nestedResolvers += this
            }

    /**
     * Creates a formal resolver by collecting formal handlers. No config used by the resolution.
     * @receiver The set of operations to handle
     * @param configure A configuration block
     * @return Returns a formal resolver
     */
    inline fun Set<String>.nestedWith(configure: FormalHandlerCollector<Unit>.() -> Unit) =
            FormalNestedResolver(Unit, this, FormalHandlerCollector<Unit>().apply(configure).handlers.toSet()).apply {
                nestedResolvers += this
            }

    /**
     * Creates a formal resolver by collecting formal handlers. No config used by the resolution.
     * @receiver The operation to handle
     * @param configure A configuration block
     * @return Returns a formal resolver
     */
    inline fun String.nestedWith(configure: FormalHandlerCollector<Unit>.() -> Unit) =
            FormalNestedResolver(Unit, setOf(this), FormalHandlerCollector<Unit>().apply(configure).handlers.toSet()).apply {
                nestedResolvers += this
            }
}


data class UndefinedOperation(
        val message: String,
        val formalContext: FormalContext,
        val context: Content,
        val op: String) : Problem

/**
 * A formal context that resolves based on given root and nested resolvers.
 * @param global The root or global content
 * @param rootResolvers The root resolvers
 * @param nestedResolvers The nested resolvers
 */
class FormalContext(global: Content,
                    val rootResolvers: Set<FormalRootResolver<*>>,
                    val nestedResolvers: Set<FormalNestedResolver<*>>) : URAContext(global) {
    /**
     * Root resolvers mapped by their operation key.
     */
    val rootResolversByOps by lazy {
        rootResolvers.flatMap { r ->
            r.ops.map { it to r }
        }.associate { it }
    }

    /**
     * Nested resolvers mapped by their operation key.
     */
    val nestedResolversByOps by lazy {
        nestedResolvers.flatMap { r ->
            r.ops.map { it to r }
        }.associate { it }
    }

    override fun operation(context: Content, op: String): Choice<URAResolver, Problem> {
        // Handle root context
        if (context.isEmpty())
            return rootResolversByOps[op].orUnit.mapRight {
                UndefinedOperation("Undefined root operation $op", this, context, op)
            }

        // Otherwise handle nested context
        return nestedResolversByOps[op].orUnit.mapRight {
            UndefinedOperation("Undefined nested operation $op", this, context, op)
        }
    }

}

inline infix fun Content.contextWith(configure: FormalResolverCollector.() -> Unit) =
        FormalResolverCollector().apply(configure).let {
            FormalContext(this, it.rootResolvers.toSet(), it.nestedResolvers.toSet())
        }

inline fun contextWith(configure: FormalResolverCollector.() -> Unit) =
        FormalResolverCollector().apply(configure).let {
            FormalContext(Content.empty, it.rootResolvers.toSet(), it.nestedResolvers.toSet())
        }

fun main(args: Array<String>) {
    class GenericProblem(val what: String) : Problem

    val c = contextWith {
        setOf("http", "https").rootWith { u: URI ->
            // Get a connection to the
            val x = u.toURL().openConnection() as? HttpURLConnection

            if (x == null)
                right(GenericProblem("Unable to open $u as HTTP connection"))
            else
                mime(x.contentType).orUnit mapLeft {
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

        "select".nestedWith {
            // TODO Arbitrary matches, e.g. "text/*"
            "text/html" idVia { u: URI, str: String ->
                u.matches(Regex("""arg:line-(\d+)-(\d+)""")) { (s, t) ->
                    val start = s.toInt()
                    val end = t.toInt()

                    str.lines().subList(start - 1, end).joinToString(System.lineSeparator())
                }.matches(Regex("""arg:chars-(\d+)-(\d+)""")) { (s, t) ->
                    val start = s.toInt()
                    val end = t.toInt()

                    str.substring(start - 1, end)
                }.mapRight {
                    GenericProblem("Illegal format for URI $u")
                }
            }
        }
    }

    println(c.resolve(uraFrom("http://www.google.de", "select", "arg:line-1-1")).mapLeft(Content::materialize))
    println(c.resolve(uraFrom("http://www.google.de", "select", "arg:line-1-2")).mapLeft(Content::materialize))
    println(c.resolve(uraFrom("http://www.google.de", "select", "arg:line-2-2")).mapLeft(Content::materialize))
    println(c.resolve(uraFrom("http://www.google.de", "select", "arg:chars-100-110")).mapLeft(Content::materialize))

}