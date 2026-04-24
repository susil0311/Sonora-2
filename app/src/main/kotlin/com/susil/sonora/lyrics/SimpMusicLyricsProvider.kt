/*
 * Sonora Project (2026)
 * Original project attribution: Chartreux Westia (github.com/koiverse)
 * Maintained by Susil (github.com/susil0311)
 * Licensed under GPL-3.0 | see git history for contributors
 */




package com.susil.sonora.lyrics

import android.content.Context
import com.susil.sonora.constants.EnableSimpMusicLyricsKey
import com.susil.sonora.simpmusic.SimpMusicLyrics
import com.susil.sonora.utils.dataStore
import com.susil.sonora.utils.get

object SimpMusicLyricsProvider : LyricsProvider {
    override val name: String = "SimpMusic"

    override fun isEnabled(context: Context): Boolean =
        context.dataStore[EnableSimpMusicLyricsKey] ?: true

    override suspend fun getLyrics(
        id: String,
        title: String,
        artist: String,
        album: String?,
        duration: Int,
    ): Result<String> = SimpMusicLyrics.getLyrics(videoId = id, duration = duration)

    override suspend fun getAllLyrics(
        id: String,
        title: String,
        artist: String,
        album: String?,
        duration: Int,
        callback: (String) -> Unit,
    ) {
        SimpMusicLyrics.getAllLyrics(videoId = id, duration = duration, callback = callback)
    }
}

