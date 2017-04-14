package org.softlang.ura

import org.softlang.ura.util.*

/**
 * Composes two contexts of different type definitions. Requires:
 * * a transformation from primary to secondary operation space
 * * a transformation from primary to secondary argument space
 * * a binary transformation between the result spaces
 * * a transformation from secondary to primary problem space
 *
 * This composition prefers resolution of the receiver and problems of [other].
 * @param O1 The primary operation space
 * @param A1 The primary argument space
 * @param R1 The primary result space
 * @param P1 The primary problem space
 * @param O2 The secondary operation space
 * @param A2 The secondary argument space
 * @param R2 The secondary result space
 * @param P2 The secondary problem space
 * @receiver The primary context
 * @param other The secondary context
 * @param txOp The operation transformation
 * @param txArgument The argument transformation
 * @param txResult The result transformation, binary
 * @param txProblem The problem transformation
 * @return Returns context that is of the same signature as the receiver.
 */
fun <O1, A1, R1, P1, O2, A2, R2, P2> Context<O1, A1, R1, P1>.over(
        other: Context<O2, A2, R2, P2>,
        txOp: (O1) -> O2,
        txArgument: (A1) -> A2,
        txResult: Bx<R1, R2>,
        txProblem: (P2) -> P1) =
        object : Context<O1, A1, R1, P1>(global) {
            override fun operation(context: R1, op: O1) =
                    // Apply with first context
                    this@over.operation(context, op) outerMapRight {
                        // On right (problem) apply with second context
                        other.operation(txResult(context), txOp(op)) mapLeft {
                            // If second context successful, return but transform spaces
                            object : Resolver<A1, R1, P1> {
                                override fun resolve(context: R1, argument: A1): Choice<R1, P1> {
                                    return it.resolve(txResult(context), txArgument(argument)) mapLeft {
                                        txResult.reverse(it)
                                    } mapRight {
                                        txProblem(it)
                                    }
                                }
                            }
                        } mapRight {
                            // If second context also failed, return it's problem
                            txProblem(it)
                        }
                    }
        }