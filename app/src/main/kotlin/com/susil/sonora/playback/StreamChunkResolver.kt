/*
 * Sonora Project (2026)
 * Original project attribution: Chartreux Westia (github.com/koiverse)
 * Maintained by Susil (github.com/susil0311)
 * Licensed under GPL-3.0 | see git history for contributors
 */

package com.susil.sonora.playback

internal fun resolveStreamChunkLength(
    requestedLength: Long,
    position: Long,
    knownContentLength: Long?,
    chunkLength: Long,
): Long? {
    if (chunkLength <= 0L || position < 0L) return null

    val remainingLength = knownContentLength?.minus(position)?.takeIf { it > 0L }
    val resolvedLength =
        listOfNotNull(
            chunkLength,
            requestedLength.takeIf { it > 0L },
            remainingLength,
        ).minOrNull()

    return resolvedLength?.takeIf { it > 0L }
}
