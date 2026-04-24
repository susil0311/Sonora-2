/*
 * Sonora Project (2026)
 * Original project attribution: Chartreux Westia (github.com/koiverse)
 * Maintained by Susil (github.com/susil0311)
 * Licensed under GPL-3.0 | see git history for contributors
 */


package com.susil.sonora.utils

import android.content.Context
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewDatabase

fun clearPlaybackWebAuthSession(context: Context) {
    clearWebAuthStorage(context)
    val cookieManager = CookieManager.getInstance()
    cookieManager.removeSessionCookies(null)
    cookieManager.removeAllCookies(null)
    cookieManager.flush()
}

fun resetAuthWebViewSession(
    context: Context,
    webView: WebView,
    onReady: () -> Unit,
) {
    webView.stopLoading()
    webView.clearHistory()
    webView.clearFormData()
    webView.clearCache(true)
    clearWebAuthStorage(context)

    val cookieManager = CookieManager.getInstance()
    cookieManager.removeSessionCookies {
        cookieManager.removeAllCookies {
            cookieManager.flush()
            cookieManager.setAcceptCookie(true)
            cookieManager.setAcceptThirdPartyCookies(webView, true)
            onReady()
        }
    }
}

private fun clearWebAuthStorage(context: Context) {
    val appContext = context.applicationContext
    WebStorage.getInstance().deleteAllData()
    WebViewDatabase.getInstance(appContext).apply {
        clearFormData()
        clearHttpAuthUsernamePassword()
        clearUsernamePassword()
    }
}
