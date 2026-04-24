/*
 * Sonora Project (2026)
 * Original project attribution: Chartreux Westia (github.com/koiverse)
 * Maintained by Susil (github.com/susil0311)
 * Licensed under GPL-3.0 | see git history for contributors
 */




package com.susil.sonora.constants

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

const val CONTENT_TYPE_HEADER = 0
const val CONTENT_TYPE_LIST = 1
const val CONTENT_TYPE_SONG = 2
const val CONTENT_TYPE_ARTIST = 3
const val CONTENT_TYPE_ALBUM = 4
const val CONTENT_TYPE_PLAYLIST = 5

val FloatingToolbarHeight = 72.dp
val FloatingToolbarHorizontalPadding = 16.dp
val FloatingToolbarBottomPadding = 12.dp
val NavigationBarHeight = FloatingToolbarHeight
val MiniPlayerHeight = 64.dp
val MiniPlayerBottomSpacing = 8.dp // Space between MiniPlayer and NavigationBar
val QueuePeekHeight = 64.dp
val AppBarHeight = 64.dp

val ListItemHeight = 72.dp
val SuggestionItemHeight = 56.dp
val SearchFilterHeight = 48.dp
val ListThumbnailSize = 56.dp
val SmallGridThumbnailHeight = 104.dp
val GridThumbnailHeight = 128.dp
val AlbumThumbnailSize = 144.dp

val ThumbnailCornerRadius = 10.dp
val GridThumbnailCornerRadius = 8.dp
val ModernCardCornerRadius = 14.dp
val ModernCardOuterPadding = 10.dp
val ModernCardInnerPadding = 10.dp

val PlayerHorizontalPadding = 32.dp

val NavigationBarAnimationSpec = spring<Dp>(
	dampingRatio = Spring.DampingRatioMediumBouncy,
	stiffness = Spring.StiffnessLow,
)

val BottomSheetAnimationSpec = spring<Dp>(
	dampingRatio = Spring.DampingRatioNoBouncy,
	stiffness = Spring.StiffnessMedium,
)

val BottomSheetSoftAnimationSpec = spring<Dp>(
	dampingRatio = Spring.DampingRatioLowBouncy,
	stiffness = Spring.StiffnessLow,
)
