/*
 * Sonora Project (2026)
 * Original project attribution: Chartreux Westia (github.com/koiverse)
 * Maintained by Susil (github.com/susil0311)
 * Licensed under GPL-3.0 | see git history for contributors
 */




package com.susil.sonora.db.entities

import androidx.room.Embedded
import androidx.room.Relation

data class PlaylistSong(
    @Embedded val map: PlaylistSongMap,
    @Relation(
        parentColumn = "songId",
        entityColumn = "id",
        entity = SongEntity::class,
    )
    val song: Song,
)
