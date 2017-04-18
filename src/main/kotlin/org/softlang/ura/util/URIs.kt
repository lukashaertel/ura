package org.softlang.ura.util

import java.net.URI

inline fun <reified R> URI.matches(regex: Regex, match: (MatchResult.Destructured) -> R): Choice<R, URI> =
        regex.matchEntire(this.toString())?.destructured.orUnit
                .mapLeft(match)
                .mapRight { this }

inline fun <reified R> Choice<R, URI>.matches(regex: Regex, match: (MatchResult.Destructured) -> R) =
        outerMapRight {
            it.matches(regex, match)
        }