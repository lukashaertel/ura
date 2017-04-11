package org.softlang.ura

/**
 * This file is the abstract definition of a typed and nested resolution.
 */

/**
 * A node in the generic resolution tree.
 *
 * @param O The operator type
 * @param A The argument type
 * @param operator The operator to apply
 * @param argument The argument to the navigation
 * @param then The next node
 */
data class Nav<out O, out A>(val operator: O, val argument: A, val then: Nav<O, A>?)

/**
 * A problem in the resolution.
 */
interface Problem

/**
 * Resolver of navigation.
 *
 * @param A The argument type
 * @param T The *type* type
 * @param I The *instance* type
 */
interface Resolver<in A, T, I> {
    /**
     * Applies the resolver on the *type*.
     *
     * @param argument The argument to resolve
     * @param type The *type* to handle
     * @return Returns the new resolved *type* if applicable
     */
    fun type(argument: A, type: T): Choice<T, Problem>

    /**
     * Applies the resolver on the *instance*.
     *
     * @param argument The argument to resolve
     * @param type The *type* to handle
     * @param instance THe *instance* to handle
     * @return Returns the new resolved *instance*
     */
    fun instance(argument: A, type: T, instance: I): I
}


/**
 * A Context of resolution.
 */
abstract class Context<in O, in A, T, I>(val rootType: T, val rootInstance: I) {
    /**
     * Maps an operation definition to an operation.
     */
    abstract fun operation(o: O): Choice<Resolver<A, T, I>, Problem>

    /**
     * Resolves the given definition. Returns either the instance or a problem that prevented resolution.
     * @param node The definition to resolve
     * @return Returns the instance of a problem
     */
    fun resolve(node: Nav<O, A>): Choice<I, Problem> {
        fun f(t: T, i: I, node: Nav<O, A>): Choice<I, Problem> =
                // Resolve operation
                operation(node.operator) outerMapLeft { r ->
                    // Check if we are at the end of resolution
                    if (node.then != null)
                    // Handle continuation
                        r.type(node.argument, t) outerMapLeft { t2 ->
                            // Resolve the instance
                            val i2 = r.instance(node.argument, t, i)
                            f(t2, i2, node.then)
                        }
                    else
                    // Handle terminal
                        r.type(node.argument, t) mapLeft {
                            // Resolve the instance
                            r.instance(node.argument, t, i)
                        }
                }

        // Apply to root
        return f(rootType, rootInstance, node)
    }
}

/**
 * A problem of key lookup in a map.
 */
data class MapLookupProblem<K, out V>(val map: Map<K, V>, val key: K, val message: String) : Problem

/**
 * Default context with explicit root *type* and *instance* and an explicit map of operation to resolver.
 */
class DefaultContext<O, in A, T, I>(rootType: T, rootInstance: I, val resolvers: Map<O, Resolver<A, T, I>>) :
        Context<O, A, T, I>(rootType, rootInstance) {
    override fun operation(o: O) =
            resolvers[o].orUnit.mapRight {
                MapLookupProblem(resolvers, o, "Operation $o is not defined")
            }
}