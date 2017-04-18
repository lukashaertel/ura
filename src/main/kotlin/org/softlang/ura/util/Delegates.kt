package org.softlang.ura.util

import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Interfaces to wrap operator signature specification.
 */

/**
 * Abstract provider of a [ReadOnlyProperty].
 * @param T The parameter of the container to provide in
 * @param E The parameter of the property
 */
interface ReadOnlyPropertyProvider<in T, out E> {
    /**
     * The provision method.
     * @param that The *this* reference
     * @param property The property to provide for
     * @return Returns a [ReadOnlyProperty].
     */
    operator fun provideDelegate(that: T, property: KProperty<*>): ReadOnlyProperty<T, E>
}

/**
 * Abstract provider of a [ReadWriteProperty].
 * @param T The parameter of the container to provide in
 * @param E The parameter of the property
 */
interface ReadWritePropertyProvider<in T, E> {
    /**
     * The provision method.
     * @param that The *this* reference
     * @param property The property to provide for
     * @return Returns a [ReadWriteProperty].
     */
    operator fun provideDelegate(that: T, property: KProperty<*>): ReadWriteProperty<T, E>
}

/**
 * Implementation of a basic *val* property.
 * @param T The parameter of the container to provide in
 * @param E The parameter of the val
 */
class ValProperty<in T, out E>(val it: E) : ReadOnlyProperty<T, E> {
    override fun getValue(thisRef: T, property: KProperty<*>) = it
}

/**
 * Implementation of a basic *var* property.
 * @param T The parameter of the container to provide in
 * @param E The parameter of the var
 */
class VarProperty<in T, E>(var it: E) : ReadWriteProperty<T, E> {
    override fun getValue(thisRef: T, property: KProperty<*>) = it

    override fun setValue(thisRef: T, property: KProperty<*>, value: E) {
        it = value
    }
}