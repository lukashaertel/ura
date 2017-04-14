package org.softlang.ura.util

import com.google.common.collect.HashMultimap
import com.google.common.collect.ImmutableMultimap
import com.google.common.collect.Multimap
import kotlin.collections.MutableMap.MutableEntry
import kotlin.reflect.full.createInstance

/**
 * We are using Guava, therefore this file just contains extension methods.
 */

fun <K, V> emptyMultimap(): ImmutableMultimap<K, V> =
        ImmutableMultimap.of<K, V>()

/**
 * Creates an empty immutable multimap.
 * @param K The key type
 * @param V The value type
 * @return Returns a new immutable multimap.
 */
fun <K : Any, V : Any> multimapOf(): ImmutableMultimap<K, V> =
        ImmutableMultimap.of<K, V>()

/**
 * Creates an immutable multimap with the given key value assignments.
 * @param K The key type
 * @param V The value type
 * @param pairs The key to value assignments
 * @return Returns a new immutable multimap.
 */
fun <K : Any, V : Any> multimapOf(vararg pairs: Pair<K, V>): ImmutableMultimap<K, V> =
        pairs.fold(ImmutableMultimap.builder<K, V>()) { b, p ->
            b.put(p.first, p.second)
        }.build()

/**
 * Creates an empty hash multimap.
 * @param K The key type
 * @param V The value type
 * @return Returns a new hash multimap.
 */
fun <K, V> hashMultimapOf() =
        HashMultimap.create<K, V>()

/**
 * Creates a hash multimap with the given key value assignments.
 * @param K The key type
 * @param V The value type
 * @param pairs The key to value assignments
 * @return Returns a new hash multimap.
 */
fun <K, V> hashMultimapOf(vararg pairs: Pair<K, V>) =
        HashMultimap.create<K, V>().apply {
            pairs.forEach { put(it.first, it.second) }
        }

/**
 * Tests if all elements of the multimap match the [p].
 * @param K The key type
 * @param V The value type
 * @receiver The multimap to take the elements from
 * @param p The predicate
 * @return Returns true if all elements matched.
 */
inline fun <K, V> Multimap<K, V>.all(p: (MutableEntry<K, V>) -> Boolean) =
        entries().all(p)

/**
 * Tests if any elements of the multimap matches the [p].
 * @param K The key type
 * @param V The value type
 * @receiver The multimap to take the elements from
 * @param p The predicate
 * @return Returns true if any element matched.
 */
inline fun <K, V> Multimap<K, V>.any(p: (MutableEntry<K, V>) -> Boolean) =
        entries().any(p)

/**
 * Interprets the elements as an iterable.
 * @param K The key type
 * @param V The value type
 * @receiver The multimap to interpret
 * @return Returns an iterable.
 */
fun <K, V> Multimap<K, V>.asIterable() =
        entries().asIterable()

/**
 * Interprets the elements as a sequence.
 * @param K The key type
 * @param V The value type
 * @receiver The multimap to interpret
 * @return Returns a sequence.
 */
fun <K, V> Multimap<K, V>.asSequence() =
        entries().asSequence()

/**
 * Counts the elements.
 * @param K The key type
 * @param V The value type
 * @receiver The multimap to count the elements in
 * @return Returns the count.
 */
fun <K, V> Multimap<K, V>.count() =
        entries().count()

/**
 * Counts the elements matching [p].
 * @param K The key type
 * @param V The value type
 * @receiver The multimap to count the elements in
 * @param p The predicate for the elements to count
 * @return Returns the count of matching elements.
 */
inline fun <K, V> Multimap<K, V>.count(p: (MutableEntry<K, V>) -> Boolean) =
        entries().count(p)

/**
 * Filters the elements with [p], creates a new immutable multimap
 * @param K The key type
 * @param V The value type
 * @receiver The multimap to filter in
 * @param p The predicate for the elements to return
 * @return Returns a new multimap of matching elements.
 */
inline fun <K, V> Multimap<K, V>.filter(p: (MutableEntry<K, V>) -> Boolean) =
        ImmutableMultimap.copyOf(entries().filter(p))

/**
 * Filters the elements by keys with [p], creates a new immutable multimap
 * @param K The key type
 * @param V The value type
 * @receiver The multimap to filter in
 * @param p The predicate for the keys to return the elements for
 * @return Returns a new multimap of matching elements.
 */
inline fun <K, V> Multimap<K, V>.filterKeys(p: (K) -> Boolean) =
        ImmutableMultimap.copyOf(entries().filter { p(it.key) })

/**
 * Filters the elements with not [p], creates a new immutable multimap
 * @param K The key type
 * @param V The value type
 * @receiver The multimap to filter in
 * @param p The predicate for the elements not to return
 * @return Returns a new multimap of matching elements.
 */
inline fun <K, V> Multimap<K, V>.filterNot(p: (MutableEntry<K, V>) -> Boolean) =
        ImmutableMultimap.copyOf(entries().filterNot(p))

/**
 * Filters the elements with not [p], stores them in an existing mutable multimap.
 * @param K The key type
 * @param V The value type
 * @receiver The multimap to filter in
 * @param to The multimap to store the assignments in
 * @param p The predicate for the elements not to return
 * @return Returns [to].
 */
inline fun <K, V, M : Multimap<in K, in V>> Multimap<K, V>.filterNotTo(to: M, p: (MutableEntry<K, V>) -> Boolean) =
        to.apply {
            this@filterNotTo.entries().filterNot(p).forEach {
                put(it.key, it.value)
            }
        }

/**
 * Filters the elements with [p], stores them in an existing mutable multimap.
 * @param K The key type
 * @param V The value type
 * @receiver The multimap to filter in
 * @param to The multimap to store the assignments in
 * @param p The predicate for the elements to return
 * @return Returns [to].
 */
inline fun <K, V, M : Multimap<in K, in V>> Multimap<K, V>.filterTo(to: M, p: (MutableEntry<K, V>) -> Boolean) =
        to.apply {
            this@filterTo.entries().filter(p).forEach {
                put(it.key, it.value)
            }
        }

/**
 * Filters the elements by values with [test], creates a new immutable multimap
 * @param K The key type
 * @param V The value type
 * @receiver The multimap to filter in
 * @param test The predicate for the values to return the elements for
 * @return Returns a new multimap of matching elements.
 */
inline fun <K, V> Multimap<K, V>.filterValues(test: (V) -> Boolean) =
        ImmutableMultimap.copyOf(entries().filter { test(it.value) })

/**
 * Maps the elements to many result elements, returning a list of the concatenated results.
 * @param K The key type
 * @param V The value type
 * @receiver The multimap to map
 * @param f The transformation returning many elements
 * @return Returns a list of concatenated results.
 */
inline fun <K, V, R> Multimap<K, V>.flatMap(f: (MutableEntry<K, V>) -> Iterable<R>): List<R> {
    val r = mutableListOf<R>()
    for (e in entries())
        r.addAll(f(e))
    return r
}

/**
 * Maps the elements to many result elements, storing the results in an existing mutable collection.
 * @param K The key type
 * @param V The value type
 * @param M The result collection type
 * @receiver The multimap to map
 * @param to The target of the mapping
 * @param f The transformation returning many elements
 * @return Returns [to].
 */
inline fun <K, V, R, M : MutableCollection<in R>>
        Multimap<K, V>.flatMapTo(to: M, f: (MutableEntry<K, V>) -> Iterable<R>): M {
    for (e in entries())
        to.addAll(f(e))
    return to
}

/**
 * Executes the block for each element.
 * @param K The key type
 * @param V The value type
 * @receiver The multimap from which the elements are taken
 * @param block The block to execute
 */
inline fun <K, V> Multimap<K, V>.forEach(block: (MutableEntry<K, V>) -> Unit) {
    entries().forEach(block)
}

/**
 * Returns true if the multimap is not empty.
 * @receiver The multimap to check
 * @return Returns true if not empty.
 */
fun Multimap<*, *>.isNotEmpty() =
        !isEmpty

/**
 * Maps the elements, creating a new list of elements
 * @param K The key type
 * @param V The value type
 * @receiver The multimap to map
 * @param f The transformation of the elements
 * @return Returns a new list of result elements.
 */
inline fun <K, V, R> Multimap<K, V>.map(f: (MutableEntry<K, V>) -> R) =
        entries().map(f)

/**
 * Maps the keys of the multimap, returns a new immutable multimap.
 * @param K The key type
 * @param V The value type
 * @receiver The multimap to map
 * @param f The transformation of the keys
 * @return Returns a new immutable multimap.
 */
inline fun <K, V, R> Multimap<K, V>.mapKeys(f: (MutableEntry<K, V>) -> R): ImmutableMultimap<R, V> {
    val b = ImmutableMultimap.builder<R, V>()
    for (e in entries())
        b.put(f(e), e.value)
    return b.build()
}

/**
 * Maps the keys of the multimap, stores the results in an existing mutable multimap.
 * @param K The key type
 * @param V The value type
 * @receiver The multimap to map
 * @param to The target to store the new elements in
 * @param f The transformation of the keys
 * @return Returns [to].
 */
inline fun <K, V, R, M : Multimap<in R, in V>>
        Multimap<K, V>.mapKeysTo(to: M, f: (MutableEntry<K, V>) -> R): M {
    for (e in entries())
        to.put(f(e), e.value)
    return to
}

/**
 * Maps the elements to optional results, creating a new list of non-null elements
 * @param K The key type
 * @param V The value type
 * @receiver The multimap to map
 * @param f The transformation of the elements
 * @return Returns a new list of result elements.
 */
inline fun <K, V, R : Any> Multimap<K, V>.mapNotNull(f: (MutableEntry<K, V>) -> R?) =
        entries().mapNotNull(f)

/**
 * Maps the elements to optional results, storing non-null elements in an existing mutable collection.
 * @param K The key type
 * @param V The value type
 * @receiver The multimap to map
 * @oaram to The target to store the elements in
 * @param f The transformation of the elements
 * @return Returns [to].
 */
inline fun <K, V, R : Any, M : MutableCollection<R>>
        Multimap<K, V>.mapNotNullTo(to: M, f: (MutableEntry<K, V>) -> R?) =
        entries().mapNotNullTo(to, f)

/**
 * Maps the elements, storing elements in an existing mutable collection.
 * @param K The key type
 * @param V The value type
 * @receiver The multimap to map
 * @oaram to The target to store the elements in
 * @param f The transformation of the elements
 * @return Returns [to].
 */
inline fun <K, V, R, M : MutableCollection<R>> Multimap<K, V>.mapTo(to: M, f: (MutableEntry<K, V>) -> R) =
        entries().mapTo(to, f)

/**
 * Maps the values of the multimap, returns a new immutable multimap.
 * @param K The key type
 * @param V The value type
 * @receiver The multimap to map
 * @param f The transformation of the values
 * @return Returns a new immutable multimap.
 */
inline fun <K, V, R> Multimap<K, V>.mapValues(f: (MutableEntry<K, V>) -> R): ImmutableMultimap<K, R> {
    val b = ImmutableMultimap.builder<K, R>()
    for (e in entries())
        b.put(e.key, f(e))
    return b.build()
}

/**
 * Maps the values of the multimap, stores the results in an existing mutable multimap.
 * @param K The key type
 * @param V The value type
 * @receiver The multimap to map
 * @param to The target to store the new elements in
 * @param f The transformation of the values
 * @return Returns [to].
 */
inline fun <K, V, R, M : Multimap<in K, in R>>
        Multimap<K, V>.mapValuesTo(to: M, f: (MutableEntry<K, V>) -> R): M {
    for (e in entries())
        to.put(e.key, f(e))
    return to
}

/**
 * Finds the maximum element for the result of the selector.
 * @param K The key type
 * @param V The value type
 * @receiver The multimap to find the max in.
 * @param f The selector.
 * @return Returns the optional maximum element.
 */
inline fun <K, V, R : Comparable<R>> Multimap<K, V>.maxBy(f: (MutableEntry<K, V>) -> R) =
        entries().maxBy(f)


/**
 * Finds the maximum element for the result using a comparator.
 * @param K The key type
 * @param V The value type
 * @receiver The multimap to find the max in.
 * @param comparator The comparator.
 * @return Returns the optional maximum element.
 */
fun <K, V> Multimap<K, V>.maxWith(comparator: Comparator<in MutableEntry<K, V>>) =
        entries().maxWith(comparator)

/**
 * Finds the minimum element for the result of the selector.
 * @param K The key type
 * @param V The value type
 * @receiver The multimap to find the min in.
 * @param f The selector.
 * @return Returns the optional minimum element.
 */
inline fun <K, V, R : Comparable<R>> Multimap<K, V>.minBy(f: (MutableEntry<K, V>) -> R) =
        entries().minBy(f)

/**
 * Finds the minimum element for the result using a comparator.
 * @param K The key type
 * @param V The value type
 * @receiver The multimap to find the min in.
 * @param comparator The comparator.
 * @return Returns the optional minimum element.
 */
fun <K, V> Multimap<K, V>.minWith(comparator: Comparator<in MutableEntry<K, V>>) =
        entries().minWith(comparator)

/**
 * Returns true if there are no elements in the multimap.
 * @receiver The multimap to check
 * @return Returns true if no element in the multimap.
 */
fun Multimap<*, *>.none() =
        entries().none()

/**
 * Returns true if there are no elements matching the predicate in the multimap.
 * @param K The key type
 * @param V The value type
 * @receiver The multimap to check
 * @param p The predicate to test
 * @return Returns true if no element in the multimap matches the predicate .
 */
inline fun <K, V> Multimap<K, V>.none(p: (MutableEntry<K, V>) -> Boolean) =
        entries().none(p)

/**
 * Applies the action on all entries, returning the receiver.
 * @param K The key type
 * @param V The value type
 * @receiver The multimap to apply on and return
 * @param action The action to apply
 * @return Returns the receiver.
 */
inline fun <K, V, M : Multimap<K, V>> M.onEach(action: (MutableEntry<K, V>) -> Unit) =
        apply { entries().forEach(action) }

/**
 * Returns the map or an empty map if null.
 * @param K The key type
 * @param V The value type
 * @receiver The multimap or null
 * @return Returns the map or an empty map if null.
 */
fun <K, V> Multimap<K, V>?.orEmpty() =
        this ?: emptyMultimap()

/**
 * Maps the multimap to a list of pairs.
 * @param K The key type
 * @param V The value type
 * @receiver The multimap to convert
 * @return Returns a list of pairs.
 */
fun <K, V> Multimap<K, V>.toList() =
        map { it.key to it.value }

/**
 * Converts the multimap to a map of keys to lists of values.
 * @param K The key type
 * @param V The value type
 * @receiver The multimap to convert
 * @return Returns a new map.
 */
fun <K, V> Multimap<K, V>.toMap(): Map<K, List<V>> =
        entries().groupBy({ it.key }) { it.value }

/**
 * Converts the multimap to a map of keys to lists of values, storing them in an existing map of keys to mutable
 * collections of values. May only create basic types as [E].
 * @param K The key type
 * @param V The value type
 * @param E The type of the mutable collection as values of the result
 * @param M The type of the mutable map
 * @receiver The multimap to convert
 * @param to The target of the mapping
 * @return Returns [to].
 */
inline fun <K, V, reified E : MutableCollection<V>, M : MutableMap<K, E>>
        Multimap<K, V>.toMap(to: M): M {
    // `Create` method, blissfully ignorant around most types, but common types are supported
    val create: (() -> E) = when (E::class) {
    // Some sort of list, do a list
        List::class, MutableList::class -> { -> mutableListOf<V>() as E }
    // A Set?
        Set::class, MutableSet::class -> { -> mutableSetOf<V>() as E }
    // Try to create a new instance
        else -> { -> E::class.createInstance() }
    }

    return toMap(to, create)
}

/**
 * Converts the multimap to a map of keys to lists of values, storing them in an existing map of keys to mutable
 * collections of values. Creates [E] by a given provider.
 * @param K The key type
 * @param V The value type
 * @param E The type of the mutable collection as values of the result
 * @param M The type of the mutable map
 * @receiver The multimap to convert
 * @param to The target of the mapping
 * @param create The provider of mutable collections as values of the result
 * @return Returns [to].
 */
inline fun <K, V, reified E : MutableCollection<V>, M : MutableMap<K, E>>
        Multimap<K, V>.toMap(to: M, create: () -> E): M {
    for ((key, value) in entries())
        to.getOrPut(key, create).add(value)

    return to
}

/**
 * Checks if the key value assignment is contained in the multimap.
 * @param K The key type
 * @param V The value type
 * @receiver The multimap to check
 * @param pair The pair to check for being element
 * @return Returns true if the key value assignment is contained in the multimap.
 */
@kotlin.jvm.JvmName("containsPair")
inline operator fun <reified K, reified V> Multimap<K, V>.contains(pair: Pair<K, V>) =
        containsEntry(pair.first, pair.second)

/**
 * Checks if the key value assignment is contained in the multimap.
 * @param K The key type
 * @param V The value type
 * @receiver The multimap to check
 * @param pair The pair to check for being element
 * @return Returns true if the key value assignment is contained in the multimap.
 */
@kotlin.jvm.JvmName("containsEntry")
inline operator fun <reified K, reified V> Multimap<K, V>.contains(pair: MutableEntry<K, V>) =
        containsEntry(pair.key, pair.value)

/**
 * Checks if the key is contained in the multimap.
 * @param K The key type
 * @receiver The multimap to check
 * @param key The key to  check for being element
 * @return Returns true if the key is contained in the multimap.
 */
@kotlin.jvm.JvmName("containsKey")
inline operator fun <reified K> Multimap<K, *>.contains(key: K) =
        containsKey(key)

/**
 * Checks if the value is contained in the multimap.
 * @param V The value type
 * @receiver The multimap to check
 * @param value The value to  check for being element
 * @return Returns true if the value is contained in the multimap.
 */
@kotlin.jvm.JvmName("containsValue")
inline operator fun <reified V> Multimap<*, V>.contains(value: V) =
        containsValue(value)

/**
 * Adds the assignment to the multimap creating a new, immutable multimap.
 * @param K The key type
 * @param V The value type
 * @receiver The multimap to add to
 * @param pair The assignment to add
 * @return Returns a new, immutable multimap.
 */
inline operator fun <reified K, reified V> Multimap<K, V>.plus(pair: Pair<K, V>): ImmutableMultimap<K, V> =
        ImmutableMultimap.builder<K, V>().putAll(this).put(pair.first, pair.second).build()

/**
 * Adds the assignments to the multimap creating a new, immutable multimap.
 * @param K The key type
 * @param V The value type
 * @receiver The multimap to add to
 * @param other The multimap to add
 * @return Returns a new, immutable multimap.
 */
inline operator fun <reified K, reified V> Multimap<K, V>.plus(other: Multimap<K, V>): ImmutableMultimap<K, V> =
        ImmutableMultimap.builder<K, V>().putAll(this).putAll(other).build()

/**
 * Adds the assignment to a mutable multimap.
 * @param K The key type
 * @param V The value type
 * @receiver The multimap to add to
 * @param pair The assignment to add.
 */
inline operator fun <reified K, reified V> Multimap<K, V>.plusAssign(pair: Pair<K, V>) {
    put(pair.first, pair.second)
}

/**
 * Adds the assignments to a mutable multimap.
 * @param K The key type
 * @param V The value type
 * @receiver The multimap to add to
 * @param other The multimap to add.
 */
inline operator fun <reified K, reified V> Multimap<K, V>.plusAssign(other: Multimap<K, V>) {
    putAll(other)
}


/**
 * Removes the assignment from the multimap creating a new, immutable multimap.
 * @param K The key type
 * @param V The value type
 * @receiver The multimap to remove from
 * @param pair The assignment to remove
 * @return Returns a new, immutable multimap.
 */
inline operator fun <reified K, reified V> Multimap<K, V>.minus(pair: Pair<K, V>): ImmutableMultimap<K, V> {
    val b = ImmutableMultimap.builder<K, V>()
    for (e in entries())
        if (e.key != pair.first && e.value != pair.second)
            b.put(e)
    return b.build()
}

/**
 * Removes the assignments from the multimap creating a new, immutable multimap.
 * @param K The key type
 * @param V The value type
 * @receiver The multimap to remove from
 * @param other The multimap to remove
 * @return Returns a new, immutable multimap.
 */
inline operator fun <reified K, reified V> Multimap<K, V>.minus(other: Multimap<K, V>): ImmutableMultimap<K, V> {
    val b = ImmutableMultimap.builder<K, V>()
    for (e in entries())
        if (e !in other)
            b.put(e)
    return b.build()
}

/**
 * Removes the assignment from a mutable multimap.
 * @param K The key type
 * @param V The value type
 * @receiver The multimap to remove from
 * @param pair The assignment to remove.
 */
inline operator fun <reified K, reified V> Multimap<K, V>.minusAssign(pair: Pair<K, V>) {
    remove(pair.first, pair.second)
}

/**
 * Removes the assignments from a mutable multimap.
 * @param K The key type
 * @param V The value type
 * @receiver The multimap to remove from
 * @param other The multimap to remove.
 */
inline operator fun <reified K, reified V> Multimap<K, V>.minusAssign(other: Multimap<K, V>) {
    other.forEach { (k, v) ->
        remove(k, v)
    }
}

/**
 * Associates the values in the iterable to key value assignments, creates a multimap.
 * @param T The source type
 * @param K The key type
 * @param V The value type
 * @receiver The iterable to take the elements from
 * @param f The transformation from element to key value assignment
 * @return Returns an immutable multimap.
 */
inline fun <T, K, V> Iterable<T>.associateAll(f: (T) -> Pair<K, V>): ImmutableMultimap<K, V> {
    val b = ImmutableMultimap.builder<K, V>()
    for (item in this)
        f(item).let { b.put(it.first, it.second) }
    return b.build()
}

/**
 * Associates the values in the iterable by a key selector, creates a multimap.
 * @param T The source type
 * @param K The key type
 * @receiver The iterable to take the elements from
 * @param key The transformation from element to key
 * @return Returns an immutable multimap.
 */
inline fun <T, K> Iterable<T>.associateAllBy(key: (T) -> K): ImmutableMultimap<K, T> {
    val b = ImmutableMultimap.builder<K, T>()
    for (item in this)
        b.put(key(item), item)
    return b.build()
}

/**
 * Associates the values in the iterable to key value assignments, creates a multimap.
 * @param T The source type
 * @param K The key type
 * @param V The value type
 * @receiver The iterable to take the elements from
 * @param key The transformation from element to key
 * @param value The transformation from element to value
 * @return Returns an immutable multimap.
 */
inline fun <T, K, V> Iterable<T>.associateAllBy(key: (T) -> K, value: (T) -> V): ImmutableMultimap<K, V> {
    val b = ImmutableMultimap.builder<K, V>()
    for (item in this)
        b.put(key(item), value(item))
    return b.build()
}

/**
 * Associates the values in the iterable by a key selector, stores them in an existing mutable multimap.
 * @param T The source type
 * @param K The key type
 * @receiver The iterable to take the elements from
 * @param to The multimap to store the assignments in
 * @param key The transformation from element to key
 * @return Returns [to].
 */
inline fun <T, K, M : Multimap<in K, in T>> Iterable<T>.associateAllByTo(to: M, key: (T) -> K): M {
    for (element in this)
        to.put(key(element), element)
    return to
}

/**
 * Associates the values in the iterable to key value assignments, stores them in an existing mutable multimap.
 * @param T The source type
 * @param K The key type
 * @param V The value type
 * @receiver The iterable to take the elements from
 * @param to The multimap to store the assignments in
 * @param key The transformation from element to key
 * @param value The transformation from element to value
 * @return Returns [to].
 */
inline fun <T, K, V, M : Multimap<in K, in V>> Iterable<T>.associateAllByTo(to: M, key: (T) -> K, value: (T) -> V): M {
    for (element in this)
        to.put(key(element), value(element))
    return to
}

/**
 * Associates the values in the iterable to key value assignments, stores them in an existing mutable multimap.
 * @param T The source type
 * @param K The key type
 * @param V The value type
 * @receiver The iterable to take the elements from
 * @param to The multimap to store the assignments in
 * @param f The transformation from element to key value assignment
 * @return Returns [to].
 */
inline fun <T, K, V, M : Multimap<in K, in V>> Iterable<T>.associateAllTo(to: M, f: (T) -> Pair<K, V>): M {
    for (element in this) {
        val (k, v) = f(element)
        to.put(k, v)
    }
    return to
}

fun main(args: Array<String>) {
    val x = hashMultimapOf(1 to "Hallo", 1 to "Echo", 2 to "World")
    println(x)
    x += 2 to "Sergal"
    println(x)
    x -= 1 to "Echo"
    println(x)

    println("Sergal" in x)
}