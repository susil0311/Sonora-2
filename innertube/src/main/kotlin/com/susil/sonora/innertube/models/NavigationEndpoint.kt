/*
 * Sonora Project (2026)
 * Original project attribution: Chartreux Westia (github.com/koiverse)
 * Maintained by Susil (github.com/susil0311)
 * Licensed under GPL-3.0 | see git history for contributors
 */




package com.susil.sonora.innertube.models

import kotlinx.serialization.Serializable

@Serializable
data class NavigationEndpoint(
    val watchEndpoint: WatchEndpoint? = null,
    val watchPlaylistEndpoint: WatchEndpoint? = null,
    val browseEndpoint: BrowseEndpoint? = null,
    val searchEndpoint: SearchEndpoint? = null,
    val queueAddEndpoint: QueueAddEndpoint? = null,
    val shareEntityEndpoint: ShareEntityEndpoint? = null,
) {
    val endpoint: Endpoint?
        get() =
            watchEndpoint
                ?: watchPlaylistEndpoint
                ?: browseEndpoint
                ?: searchEndpoint
                ?: queueAddEndpoint
                ?: shareEntityEndpoint
    
    val anyWatchEndpoint: WatchEndpoint?
        get() = watchEndpoint
            ?: watchPlaylistEndpoint
}
