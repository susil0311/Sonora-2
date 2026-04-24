/*
 * Sonora Project (2026)
 * Original project attribution: Chartreux Westia (github.com/koiverse)
 * Maintained by Susil (github.com/susil0311)
 * Licensed under GPL-3.0 | see git history for contributors
 */

package com.susil.sonora.viewmodels

import android.content.Context
import android.net.ConnectivityManager
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.susil.sonora.constants.AudioQuality
import com.susil.sonora.constants.AudioQualityKey
import com.susil.sonora.constants.NetworkMeteredKey
import com.susil.sonora.constants.PlayerStreamClient
import com.susil.sonora.constants.PlayerStreamClientKey
import com.susil.sonora.db.MusicDatabase
import com.susil.sonora.db.entities.FormatEntity
import com.susil.sonora.models.MediaMetadata
import com.susil.sonora.utils.RingtoneHelper
import com.susil.sonora.utils.YTPlayerUtils
import com.susil.sonora.utils.dataStore
import com.susil.sonora.utils.get
import com.susil.sonora.utils.isLocalMediaId
import com.susil.sonora.utils.retryWithoutPlaybackLoginContext
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class RingtoneUiState(
    val targetMetadata: MediaMetadata? = null,
    val showTrimmer: Boolean = false,
    val showProgress: Boolean = false,
    val progress: Float = 0f,
    val statusMessage: String = "",
    val isComplete: Boolean = false,
    val isSuccess: Boolean = false,
    val ringtoneUri: Uri? = null,
)

@HiltViewModel
class RingtoneViewModel
@Inject
constructor(
    private val database: MusicDatabase,
    private val ringtoneHelper: RingtoneHelper,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RingtoneUiState())
    val uiState: StateFlow<RingtoneUiState> = _uiState.asStateFlow()

    fun openTrimmer(mediaMetadata: MediaMetadata) {
        _uiState.update {
            it.copy(
                targetMetadata = mediaMetadata,
                showTrimmer = true,
            )
        }
    }

    fun dismissTrimmer() {
        _uiState.update { it.copy(showTrimmer = false) }
    }

    fun dismissProgress() {
        _uiState.update { it.copy(showProgress = false) }
    }

    fun hasWriteSettingsPermission(): Boolean = ringtoneHelper.hasWriteSettingsPermission()

    fun openWriteSettingsPage() {
        ringtoneHelper.openWriteSettingsPage()
    }

    fun openRingtonePicker(uri: Uri?) {
        ringtoneHelper.openRingtonePicker(uri)
    }

    suspend fun resolvePreviewUri(mediaMetadata: MediaMetadata): Uri? {
        val mediaId = mediaMetadata.id
        if (mediaId.isLocalMediaId()) {
            return mediaId.toUri()
        }

        val localFormat = withContext(Dispatchers.IO) {
            database.format(mediaId).asStateFlowSnapshot()
        }

        localFormat?.playbackUrl?.takeIf { it.isNotBlank() }?.let {
            return it.toUri()
        }

        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val audioQuality =
            context.dataStore.get(AudioQualityKey, AudioQuality.AUTO.name)
                .let { runCatching { AudioQuality.valueOf(it) }.getOrDefault(AudioQuality.AUTO) }

        val preferredStreamClient =
            context.dataStore.get(PlayerStreamClientKey, PlayerStreamClient.ANDROID_VR.name)
                .let { runCatching { PlayerStreamClient.valueOf(it) }.getOrDefault(PlayerStreamClient.ANDROID_VR) }

        val networkMetered = context.dataStore.get(NetworkMeteredKey)

        val playbackDataResult =
            context.retryWithoutPlaybackLoginContext {
                YTPlayerUtils.playerResponseForPlayback(
                    videoId = mediaId,
                    audioQuality = audioQuality,
                    connectivityManager = connectivityManager,
                    preferredStreamClient = preferredStreamClient,
                    networkMetered = networkMetered,
                )
            }

        return playbackDataResult.getOrNull()?.streamUrl?.toUri()
    }

    fun setAsRingtone(
        mediaMetadata: MediaMetadata,
        startMs: Long,
        endMs: Long,
    ) {
        _uiState.update {
            it.copy(
                showTrimmer = false,
                showProgress = true,
                progress = 0f,
                statusMessage = context.getString(com.susil.sonora.R.string.ringtone_status_starting),
                isComplete = false,
                isSuccess = false,
                ringtoneUri = null,
            )
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    progress = 0.1f,
                    statusMessage = context.getString(com.susil.sonora.R.string.ringtone_status_resolving),
                )
            }
            val sourceUri = resolvePreviewUri(mediaMetadata)
            if (sourceUri == null) {
                _uiState.update {
                    it.copy(
                        isComplete = true,
                        isSuccess = false,
                        progress = 1f,
                        statusMessage = context.getString(com.susil.sonora.R.string.ringtone_error_no_source),
                    )
                }
                return@launch
            }

            _uiState.update {
                it.copy(
                    progress = 0.35f,
                    statusMessage = context.getString(com.susil.sonora.R.string.ringtone_status_trimming),
                )
            }
            val result =
                ringtoneHelper.trimAndSaveAsRingtone(
                    sourceUri = sourceUri,
                    songId = mediaMetadata.id,
                    songTitle = mediaMetadata.title,
                    artistName = mediaMetadata.artists.joinToString { artist -> artist.name },
                    startMs = startMs,
                    endMs = endMs,
                )

            result.onSuccess { uri ->
                _uiState.update {
                    it.copy(
                        isComplete = true,
                        isSuccess = true,
                        progress = 1f,
                        statusMessage = context.getString(com.susil.sonora.R.string.ringtone_success_saved),
                        ringtoneUri = uri,
                    )
                }
            }.onFailure {
                _uiState.update {
                    it.copy(
                        isComplete = true,
                        isSuccess = false,
                        progress = 1f,
                        statusMessage = it.statusMessage.ifBlank {
                            context.getString(com.susil.sonora.R.string.ringtone_error_save_failed)
                        },
                    )
                }
            }
        }
    }

    private suspend fun <T> kotlinx.coroutines.flow.Flow<T>.asStateFlowSnapshot(): T? {
        return withContext(Dispatchers.IO) {
            runCatching { first() }.getOrNull()
        }
    }
}
