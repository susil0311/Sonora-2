/*
 * Sonora Project (2026)
 * Original project attribution: Chartreux Westia (github.com/koiverse)
 * Maintained by Susil (github.com/susil0311)
 * Licensed under GPL-3.0 | see git history for contributors
 */

package com.susil.sonora.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class RingtoneHelper
@Inject
constructor(
    @ApplicationContext private val appContext: Context,
) {
    suspend fun trimAndSaveAsRingtone(
        sourceUri: Uri,
        songId: String,
        songTitle: String,
        artistName: String,
        startMs: Long,
        endMs: Long,
    ): Result<Uri> =
        withContext(Dispatchers.IO) {
            runCatching {
                val safeStart = startMs.coerceAtLeast(0L)
                val safeEnd = endMs.coerceAtLeast(safeStart + 500L)

                val sourceFile = File(appContext.cacheDir, "ringtone_source_${songId.hashCode()}.tmp")
                val trimmedFile = File(appContext.cacheDir, "ringtone_trimmed_${songId.hashCode()}.m4a")

                sourceFile.delete()
                trimmedFile.delete()

                appContext.contentResolver.openInputStream(sourceUri)?.use { input ->
                    sourceFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                } ?: error("Unable to open ringtone source")

                val trimmed = trimAudio(sourceFile, trimmedFile, safeStart, safeEnd)
                if (!trimmed || !trimmedFile.exists() || trimmedFile.length() <= 0L) {
                    error("Unable to trim ringtone")
                }

                saveTrimmedRingtone(
                    trimmedFile = trimmedFile,
                    songId = songId,
                    songTitle = songTitle,
                    artistName = artistName,
                )
            }.onFailure {
                Timber.e(it, "Failed to trim or save ringtone")
            }
        }

    fun hasWriteSettingsPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.System.canWrite(appContext)
        } else {
            true
        }
    }

    fun openWriteSettingsPage() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
        val intent =
            Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                data = Uri.parse("package:${appContext.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        appContext.startActivity(intent)
    }

    fun openRingtonePicker(ringtoneUri: Uri?) {
        runCatching {
            val intent =
                Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                    putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE)
                    putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                    putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                    if (ringtoneUri != null) {
                        putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, ringtoneUri)
                    }
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            appContext.startActivity(intent)
        }.onFailure {
            val fallback =
                Intent(Settings.ACTION_SOUND_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            appContext.startActivity(fallback)
        }
    }

    private suspend fun trimAudio(
        sourceFile: File,
        outputFile: File,
        startMs: Long,
        endMs: Long,
    ): Boolean =
        withContext(Dispatchers.Main) {
            suspendCancellableCoroutine { continuation ->
                val mediaItem =
                    MediaItem
                        .Builder()
                        .setUri(Uri.fromFile(sourceFile))
                        .setClippingConfiguration(
                            MediaItem.ClippingConfiguration
                                .Builder()
                                .setStartPositionMs(startMs)
                                .setEndPositionMs(endMs)
                                .build(),
                        ).build()

                val editedMediaItem =
                    EditedMediaItem
                        .Builder(mediaItem)
                        .setRemoveVideo(true)
                        .build()

                val transformer =
                    Transformer
                        .Builder(appContext)
                        .setAudioMimeType(MimeTypes.AUDIO_AAC)
                        .build()

                val listener =
                    object : Transformer.Listener {
                        override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                            if (continuation.isActive) continuation.resume(true)
                        }

                        override fun onError(
                            composition: Composition,
                            exportResult: ExportResult,
                            exportException: ExportException,
                        ) {
                            if (continuation.isActive) continuation.resume(false)
                        }
                    }

                transformer.addListener(listener)

                try {
                    transformer.start(editedMediaItem, outputFile.absolutePath)
                } catch (_: Exception) {
                    if (continuation.isActive) continuation.resume(false)
                }

                continuation.invokeOnCancellation {
                    transformer.cancel()
                }
            }
        }

    private fun saveTrimmedRingtone(
        trimmedFile: File,
        songId: String,
        songTitle: String,
        artistName: String,
    ): Uri {
        val sanitizedTitle = songTitle.replace(Regex("[^a-zA-Z0-9\\s]"), "").trim().ifBlank { "sonora" }
        val fileName = "${sanitizedTitle}_trimmed_${songId.hashCode()}.m4a"

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues =
                ContentValues().apply {
                    put(MediaStore.Audio.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Audio.Media.MIME_TYPE, "audio/mp4")
                    put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_RINGTONES)
                    put(MediaStore.Audio.Media.IS_RINGTONE, true)
                    put(MediaStore.Audio.Media.IS_NOTIFICATION, true)
                    put(MediaStore.Audio.Media.IS_ALARM, true)
                    put(MediaStore.Audio.Media.TITLE, "$songTitle (Ringtone)")
                    put(MediaStore.Audio.Media.ARTIST, artistName)
                    put(MediaStore.Audio.Media.IS_PENDING, 1)
                }

            val uri =
                appContext.contentResolver.insert(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    contentValues,
                ) ?: error("Unable to create ringtone entry")

            appContext.contentResolver.openOutputStream(uri)?.use { output ->
                trimmedFile.inputStream().use { input ->
                    input.copyTo(output)
                }
            } ?: error("Unable to write ringtone data")

            contentValues.clear()
            contentValues.put(MediaStore.Audio.Media.IS_PENDING, 0)
            appContext.contentResolver.update(uri, contentValues, null, null)

            trimmedFile.delete()
            uri
        } else {
            val ringtonesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES)
            if (!ringtonesDir.exists()) {
                ringtonesDir.mkdirs()
            }

            val target = File(ringtonesDir, fileName)
            trimmedFile.copyTo(target, overwrite = true)

            val contentValues =
                ContentValues().apply {
                    put(MediaStore.Audio.Media.DATA, target.absolutePath)
                    put(MediaStore.Audio.Media.TITLE, "$songTitle (Ringtone)")
                    put(MediaStore.Audio.Media.ARTIST, artistName)
                    put(MediaStore.Audio.Media.MIME_TYPE, "audio/mp4")
                    put(MediaStore.Audio.Media.IS_RINGTONE, true)
                    put(MediaStore.Audio.Media.IS_NOTIFICATION, true)
                    put(MediaStore.Audio.Media.IS_ALARM, true)
                }

            appContext.contentResolver.insert(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                contentValues,
            ) ?: Uri.fromFile(target)
        }
    }
}
