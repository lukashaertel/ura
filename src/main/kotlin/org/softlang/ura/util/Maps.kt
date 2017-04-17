package org.softlang.ura.util

/**
 * Checks if the map is equalMapping with the other map, i.e., no assignments differ.
 * @receiver The first operand of the operation
 * @param other The second operand of the operation
 * @return Returns true if the maps can be unified.
 */
infix fun Map<*, *>.equalMapping(other: Map<*, *>) = entries.none { (k, v) ->
    other[k].let { it != null && it != v }
}