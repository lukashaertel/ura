package org.softlang.ura

import org.softlang.ura.content.*
import org.softlang.ura.util.*
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI

// TODO: This first EDSL draft might need some heavy redesign.

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
 * @param dst The destination extended MIME, as calculated by the response
 * @param handle The handle method
 */
data class FormalHandler<in C, T : Any, U : Any>(
        val src: XMime<T>,
        val dst: (XMime<*>) -> XMime<U>,
        val handle: C.(URI, T) -> Choice<U, Problem>)

/**
 * Configurator block for a resolver, collects formal handlers using [via] and [idVia].
 */
class FormalHandlerCollector<C> {
    /**
     * Mutable set of collected handles.
     */
    val handlers = hashSetOf<FormalHandler<C, *, *>>()

    infix fun Mime.into(tx: (XMime<*>) -> Mime) =
            this to tx

    infix fun String.into(tx: (XMime<*>) -> String) =
            this to tx


    /**
     * For a pair of MIMEs, adds a formal handler to the collected handlers, inferring [T] and [U] from the context.
     * @param T The source type
     * @param U The destination type
     * @receiver A pair encapsulating source and target MIME type
     * @param handle The handle method
     */
    @kotlin.jvm.JvmName("viaMimeTx")
    inline infix fun <reified T : Any, reified U : Any>
            Pair<Mime, ((XMime<*>) -> Mime)>.via(noinline handle: C.(URI, T) -> Choice<U, Problem>) =
            FormalHandler(xMime<T>(first), { xMime<U>(second(it)) }, handle).apply {
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
    @kotlin.jvm.JvmName("viaParsedTx")
    inline infix fun <reified T : Any, reified U : Any>
            Pair<String, ((XMime<*>) -> String)>.via(noinline handle: C.(URI, T) -> Choice<U, Problem>) =
            FormalHandler(xMime<T>(first) ?: illegalArg("Source MIME format illegal"),
                    { xMime<U>(second(it)) ?: illegalArg("Destination MIME format from Tx illegal") },
                    handle).apply {
                handlers += this
            }


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
            FormalHandler(xMime<T>(first), { xMime<U>(second) }, handle).apply {
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
            FormalHandler(xMime<T>(first) ?: illegalArg("Source MIME format illegal"),
                    xMime<U>(second)?.const?.unbind() ?: illegalArg("Destination MIME format  illegal"),
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
            FormalHandler(xMime<T>(this), { xMime<T>(this) }, handle).apply {
                handlers += this
            }

    /**
     * For a MIME, adds a formal handler to the collected handlers, inferring [T] from the context. Retains the
     * response MIME instead of the source MIME.
     * @param T The source type
     * @receiver The MIME type
     * @param handle The handle method
     */
    @kotlin.jvm.JvmName("idViaRetainedParsed")
    inline infix fun <reified T : Any>
            String.idViaResponse(noinline handle: C.(URI, T) -> Choice<T, Problem>) =
            FormalHandler(xMime<T>(this) ?: illegalArg("Source MIME format illegal"),
                    { xMime<T>(it.mime) },
                    handle).apply {
                handlers += this
            }

    /**
     * For a MIME string, adds a formal handler to the collected handlers, inferring [T] from the context. Retains the
     * response MIME instead of the source MIME.
     * @param T The source type
     * @receiver The MIME type as a string
     * @param handle The handle method
     */
    @kotlin.jvm.JvmName("idViaRetainedMime")
    inline infix fun <reified T : Any>
            Mime.idViaResponse(noinline handle: C.(URI, T) -> Choice<T, Problem>) =
            FormalHandler(xMime<T>(this), { xMime<T>(it.mime) }, handle).apply {
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
            FormalHandler(xMime<T>(this) ?: illegalArg("Source MIME format illegal"),
                    xMime<T>(this)?.const?.unbind() ?: illegalArg("Destination MIME format  illegal"),
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

        // Return the provider implementation of configured provision
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
                    val item = context.with(it.src) { x: Any ->
                        // Provide the destination extended MIME
                        { arb(config, argument, x) }
                    }

                    // Compose by applying on the response type pairing with the resolved item, or null if resolved
                    // is null
                    it.dst(item.response) notNullTo item.resolved
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
    data class GenericProblem(val what: String) : Problem
    data class IOProblem(val what: String) : Problem
    data class ParameterProblem(val what: String, val uri: URI) : Problem

    val c = contextWith {
        "http".rootWith { u: URI ->
            // Get a connection to the
            val x = u.toURL().openConnection() as? HttpURLConnection

            if (x == null)
                right(IOProblem("Unable to open $u as HTTP connection"))
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
                    IOProblem("Unable to parse MIME type of the response")
                }
        }

        "lorem".rootWith { u: URI ->
            if (u.schemeSpecificPart != "-")
                right(ParameterProblem("Illegal SSP for URI ${u.schemeSpecificPart}", u))
            else
                left(content {
                    "text/plain" by {
                        """Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed et tellus ex. Nullam ultricies lorem nec elit congue faucibus. Maecenas nunc ligula, varius ac vulputate in, porta vitae arcu. Nullam dignissim malesuada mi ac gravida. Aenean sit amet sagittis ligula. Pellentesque pellentesque quam felis, sit amet eleifend urna commodo quis. Etiam non nisi leo.

Sed a nisi pulvinar, sagittis risus mattis, facilisis ante. In lacinia commodo velit non auctor. Suspendisse elementum blandit ligula. Donec blandit arcu sit amet tortor aliquam, ut auctor nisl bibendum. Quisque diam libero, fermentum rhoncus eros non, ornare condimentum risus. Phasellus placerat turpis nec dui lacinia, eu vulputate neque dignissim. Quisque nec arcu massa.

Curabitur sed orci nec mi aliquet ullamcorper ac in orci. Curabitur convallis, est ut laoreet consectetur, metus leo vulputate ex, ac dapibus lorem sem et tortor. Praesent imperdiet purus sit amet ipsum aliquam, ac faucibus augue rutrum. Nullam interdum ex sit amet elit maximus imperdiet. In pretium, ligula ac mollis vulputate, nisl felis ultrices justo, ut ultrices risus nisi ac nisi. Donec ut ligula lacus. Quisque tincidunt turpis ac purus interdum, eu dictum nibh pellentesque. Proin iaculis libero mauris, euismod ornare leo varius at. Curabitur id hendrerit leo. In quis lectus pulvinar, ultricies est a, tempor massa. Vestibulum convallis purus efficitur, semper ligula nec, bibendum ligula. Duis ultrices felis a semper laoreet. Maecenas eget laoreet quam. Fusce blandit quam sit amet elit volutpat finibus.

Sed aliquet varius lectus sed accumsan. Ut vestibulum viverra mi, vehicula vulputate justo aliquet ut. Fusce nec consequat felis. Morbi congue nunc in imperdiet lacinia. Aliquam erat volutpat. Etiam malesuada quis purus commodo mattis. Curabitur sagittis aliquam justo eu euismod. Praesent leo quam, commodo eget blandit et, elementum ut mi. Vivamus laoreet nulla leo, a aliquam libero maximus non. Ut ut pellentesque arcu. Mauris non sapien odio. Nulla maximus mollis turpis, nec ultricies lorem dignissim ut. Maecenas mattis leo nibh, nec sollicitudin justo blandit vel. Curabitur porta orci eget sem malesuada molestie.

Cras pellentesque, sapien vel suscipit blandit, nulla sapien condimentum eros, consequat mollis lectus odio nec tellus. Cras in lectus nec tellus lacinia tincidunt id at nunc. Nam sit amet consectetur diam. Maecenas aliquet enim tellus, in fermentum risus molestie porttitor. Vestibulum tempus pulvinar mauris. Aenean a commodo libero. Praesent efficitur ac tortor id placerat. Duis et ullamcorper neque. Curabitur sit amet tortor elementum, fringilla metus in, sodales mauris. Phasellus lacus orci, laoreet ac laoreet condimentum, elementum id felis. Donec sed accumsan arcu, sed ornare elit."""
                    }
                })
        }

        "select".nestedWith {
            "text/*" idViaResponse { u: URI, str: String ->
                u.matches(Regex("""arg:line-(\d+)-(\d+)""")) { (s, t) ->
                    val start = s.toInt()
                    val end = t.toInt()

                    str.lines().subList(start - 1, end).joinToString(System.lineSeparator())
                }.matches(Regex("""arg:chars-(\d+)-(\d+)""")) { (s, t) ->
                    val start = s.toInt()
                    val end = t.toInt()

                    str.substring(start - 1, end)
                }.mapRight {
                    ParameterProblem("SSP should be line-<start>-<end> or chars-<start>-<end>", u)
                }
            }
        }
    }

    println(c.resolve(uraFrom("lorem:-", "select", "arg:line-3-3")).mapLeft(Content::materialize))
    println(c.resolve(uraFrom("lorem:-", "select", "arg:chars-1-100")).mapLeft(Content::materialize))

    println(c.resolve(uraFrom("http://www.google.de", "select", "arg:line-1-1")).mapLeft(Content::materialize))
    //println(c.resolve(uraFrom("http://www.google.de", "select", "arg:line-1-2")).mapLeft(Content::materialize))
    //println(c.resolve(uraFrom("http://www.google.de", "select", "arg:line-2-2")).mapLeft(Content::materialize))
    //println(c.resolve(uraFrom("http://www.google.de", "select", "arg:chars-100-110")).mapLeft(Content::materialize))

}