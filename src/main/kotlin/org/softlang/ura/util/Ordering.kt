package org.softlang.ura.util

import com.google.common.collect.Ordering

/**
 * Utility method for [Ordering.onResultOf] that enforces non-nullity for the given handler function's parameters.
 * @param T The ordering type
 * @receiver The ordering to transform
 * @param block The block to apply on the elements
 * @return Returns a new ordering with [Ordering.onResultOf] applied.
 */
inline fun <reified T, reified U> Ordering<T>.onResult(crossinline block: (U) -> T) =
        onResultOf<U> { block(it!!) }!!