/*
 * Sonora Project (2026)
 * Original project attribution: Chartreux Westia (github.com/koiverse)
 * Maintained by Susil (github.com/susil0311)
 * Licensed under GPL-3.0 | see git history for contributors
 */




package com.susil.sonora.innertube.models

import kotlinx.serialization.Serializable

@Serializable
data class MusicResponsiveHeaderRenderer(
    val thumbnail: ThumbnailRenderer?,
    val buttons: List<Button>,
    val title: Runs,
    val subtitle: Runs,
    val secondSubtitle: Runs?,
    val straplineTextOne: Runs?
) {
    @Serializable
    data class Button(
        val musicPlayButtonRenderer: MusicPlayButtonRenderer?,
        val menuRenderer: Menu.MenuRenderer?
    ) {
        @Serializable
        data class MusicPlayButtonRenderer(
            val playNavigationEndpoint: NavigationEndpoint?,
        )
    }
}
