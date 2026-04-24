/*
 * Sonora Project (2026)
 * Original project attribution: Chartreux Westia (github.com/koiverse)
 * Maintained by Susil (github.com/susil0311)
 * Licensed under GPL-3.0 | see git history for contributors
 */




package com.susil.sonora.playback.queues

import androidx.media3.common.MediaItem
import com.susil.sonora.extensions.ExtraIsMusicVideo
import com.susil.sonora.extensions.metadata
import com.susil.sonora.models.MediaMetadata

interface Queue {
    val preloadItem: MediaMetadata?

    suspend fun getInitialStatus(): Status

    fun shouldExpandToFullQueueWhenAutoLoadMoreDisabled(): Boolean = false

    fun hasNextPage(): Boolean

    suspend fun nextPage(): List<MediaItem>

    data class Status(
        val title: String?,
        val items: List<MediaItem>,
        val mediaItemIndex: Int,
        val position: Long = 0L,
    ) {
        fun filterExplicit(enabled: Boolean = true) =
            if (enabled) {
                copy(
                    items = items.filterExplicit(),
                )
            } else {
                this
            }
        fun filterVideo(enabled: Boolean = true) =
            if (enabled) {
                copy(
                    items = items.filterVideo(),
                )
            } else {
                this
            }
    }
}

fun List<MediaItem>.filterExplicit(enabled: Boolean = true) =
    if (enabled) {
        filterNot {
            it.metadata?.explicit == true
        }
    } else {
        this
    }

fun List<MediaItem>.filterVideo(enabled: Boolean = true) =
    if (enabled) {
        filterNot {
            it.mediaMetadata.extras?.getBoolean(ExtraIsMusicVideo, false) == true
        }
    } else {
        this
    }
