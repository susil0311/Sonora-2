/*
 * Sonora Project (2026)
 * Original project attribution: Chartreux Westia (github.com/koiverse)
 * Maintained by Susil (github.com/susil0311)
 * Licensed under GPL-3.0 | see git history for contributors
 */




package com.susil.sonora.playback.queues

import androidx.media3.common.MediaItem
import com.susil.sonora.innertube.YouTube
import com.susil.sonora.innertube.models.WatchEndpoint
import com.susil.sonora.extensions.toMediaItem
import com.susil.sonora.models.MediaMetadata
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

class YouTubeQueue(
    private var endpoint: WatchEndpoint,
    override val preloadItem: MediaMetadata? = null,
    private val followAutomixPreview: Boolean = false,
    private val expandToFullQueueWhenAutoLoadMoreDisabled: Boolean = false,
) : Queue {
    private var continuation: String? = null

    override suspend fun getInitialStatus(): Queue.Status {
        val nextResult =
            withContext(IO) {
                YouTube.next(
                    endpoint = endpoint,
                    continuation = continuation,
                    followAutomixPreview = followAutomixPreview,
                ).getOrThrow()
            }
        endpoint = nextResult.endpoint
        continuation = nextResult.continuation
        return Queue.Status(
            title = nextResult.title,
            items = nextResult.items.map { it.toMediaItem() },
            mediaItemIndex = nextResult.currentIndex ?: 0,
        )
    }

    override fun hasNextPage(): Boolean = continuation != null

    override fun shouldExpandToFullQueueWhenAutoLoadMoreDisabled(): Boolean =
        expandToFullQueueWhenAutoLoadMoreDisabled

    override suspend fun nextPage(): List<MediaItem> {
        val nextResult =
            withContext(IO) {
                YouTube.next(
                    endpoint = endpoint,
                    continuation = continuation,
                    followAutomixPreview = followAutomixPreview,
                ).getOrThrow()
            }
        endpoint = nextResult.endpoint
        continuation = nextResult.continuation
        return nextResult.items.map { it.toMediaItem() }
    }

    companion object {
        fun playlist(
            endpoint: WatchEndpoint,
            preloadItem: MediaMetadata? = null,
        ) = YouTubeQueue(
            endpoint = endpoint,
            preloadItem = preloadItem,
            expandToFullQueueWhenAutoLoadMoreDisabled = true,
        )

        fun radio(song: MediaMetadata) =
            YouTubeQueue(
                endpoint = WatchEndpoint(videoId = song.id),
                preloadItem = song,
                followAutomixPreview = true,
            )
    }
}
