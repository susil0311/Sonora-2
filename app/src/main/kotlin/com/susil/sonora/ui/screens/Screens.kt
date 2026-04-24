/*
 * Sonora Project (2026)
 * Original project attribution: Chartreux Westia (github.com/koiverse)
 * Maintained by Susil (github.com/susil0311)
 * Licensed under GPL-3.0 | see git history for contributors
 */




package com.susil.sonora.ui.screens

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.susil.sonora.R

@Immutable
sealed class Screens(
    @StringRes val titleId: Int,
    @DrawableRes val iconIdInactive: Int,
    @DrawableRes val iconIdActive: Int,
    val route: String,
) {
    object Home : Screens(
        titleId = R.string.home,
        iconIdInactive = R.drawable.home_outlined,
        iconIdActive = R.drawable.home_filled,
        route = "home"
    )

    object Search : Screens(
        titleId = R.string.search,
        iconIdInactive = R.drawable.search,
        iconIdActive = R.drawable.search,
        route = "search"
    )

    object Library : Screens(
        titleId = R.string.filter_library,
        iconIdInactive = R.drawable.library_outlined,
        iconIdActive = R.drawable.library_filled,
        route = "library"
    )

    object MoodAndGenres : Screens(
        titleId = R.string.mood_and_genres,
        iconIdInactive = R.drawable.style,
        iconIdActive = R.drawable.style,
        route = "mood_and_genres"
    )

    object FullscreenVideo : Screens(
        titleId = R.string.now_playing,
        iconIdInactive = R.drawable.play,
        iconIdActive = R.drawable.play,
        route = "fullscreen_video"
    )

    companion object {
        val MainScreens = listOf(Home, Search, MoodAndGenres, Library)
        val TvMainScreens = listOf(Home, Search, Library)
    }
}
