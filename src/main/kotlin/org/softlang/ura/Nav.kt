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
 * Resolver of navigation.
 *
 * @param A The argument type
 * @param R The result type
 * @param P The problem type
 */
interface Resolver<in A, R, out P> {
    fun resolve(context: R, argument: A): Choice<R, P>
}


/**
 * A Context of resolution.
 * @param O The operation type
 * @param A The argument type
 * @param R The result type
 * @param P The problem type
 * @param global The global instance, passed as context to the first resolution
 */
abstract class Context<in O, in A, R, out P>(val global: R) {
    /**
     * Maps an operation definition in a context to an operation.
     */
    abstract fun operation(context: R, o: O): Choice<Resolver<A, R, P>, P>

    /**
     * Resolves the given definition. Returns either the instance or a problem that prevented resolution.
     * @param node The definition to resolve
     * @return Returns the instance or a problem
     */
    fun resolve(node: Nav<O, A>): Choice<R, P> {
        fun f(c: R, node: Nav<O, A>): Choice<R, P> =
                operation(c, node.operator) outerMapLeft {
                    // After successful resolution, handle continuation if there's more nodes after the current
                    if (node.then != null)
                        it.resolve(c, node.argument) outerMapLeft { f(it, node.then) }
                    else
                        it.resolve(c, node.argument)
                }

        // Apply to root
        return f(global, node)
    }
}