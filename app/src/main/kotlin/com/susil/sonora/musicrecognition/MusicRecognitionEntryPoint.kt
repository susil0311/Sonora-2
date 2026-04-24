/*
 * Sonora Project (2026)
 * Original project attribution: Chartreux Westia (github.com/koiverse)
 * Maintained by Susil (github.com/susil0311)
 * Licensed under GPL-3.0 | see git history for contributors
 */

package com.susil.sonora.musicrecognition

import androidx.navigation.NavHostController

const val MusicRecognitionRoute = "music_recognition"
const val ACTION_MUSIC_RECOGNITION = "com.susil.sonora.action.MUSIC_RECOGNITION"
const val MusicRecognitionAutoStartRequestKey = "music_recognition_auto_start_request"

fun NavHostController.openMusicRecognition(
    autoStartRequestId: Long = System.currentTimeMillis(),
) {
    val currentRoute = currentDestination?.route
    if (currentRoute != MusicRecognitionRoute && !popBackStack(MusicRecognitionRoute, inclusive = false)) {
        navigate(MusicRecognitionRoute) {
            launchSingleTop = true
        }
    }

    getBackStackEntry(MusicRecognitionRoute).savedStateHandle[MusicRecognitionAutoStartRequestKey] =
        autoStartRequestId
}
