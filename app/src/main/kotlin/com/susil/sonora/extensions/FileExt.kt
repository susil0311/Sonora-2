/*
 * Sonora Project (2026)
 * Original project attribution: Chartreux Westia (github.com/koiverse)
 * Maintained by Susil (github.com/susil0311)
 * Licensed under GPL-3.0 | see git history for contributors
 */




package com.susil.sonora.extensions

import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

operator fun File.div(child: String): File = File(this, child)

fun File.directorySizeBytes(): Long {
    if (!exists()) return 0L
    return walkTopDown()
        .filter { it.isFile }
        .sumOf { it.length() }
}

fun InputStream.zipInputStream(): ZipInputStream = ZipInputStream(this)

fun OutputStream.zipOutputStream(): ZipOutputStream = ZipOutputStream(this)
