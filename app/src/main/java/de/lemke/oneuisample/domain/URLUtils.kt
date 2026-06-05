@file:Suppress("unused")

package de.lemke.oneuisample.domain

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.util.Log
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import de.lemke.oneuisample.R
import java.net.URLEncoder

private const val TAG = "URLUtils"

/** Returns this string with an `https://` prefix, unless it already starts with `http://` or `https://`. */
fun String.withHttps() = if (this.startsWith("http://") || this.startsWith("https://")) this else "https://$this"

/** Removes `https://`, `http://`, and a trailing `/` from this string. */
fun String.withoutHttps() = this.removePrefix("https://").removePrefix("http://").removeSuffix("/")

/** Replaces `&` with its percent-encoded form `%26` to prevent URL parameter splitting. */
fun String.urlEncodeAmpersand() = this.replace("&", "%26")

/** Percent-encodes this string using UTF-8. */
fun String.urlEncode(): String = URLEncoder.encode(this, "UTF-8")

/** Opens [url] in the default browser, showing a toast if no browser is available or the URL is blank. */
fun Fragment.openURL(url: String?): Boolean = requireContext().openURL(url)

/** Opens [url] in the default browser, showing a toast if no browser is available or the URL is blank. */
@Suppress("TooGenericExceptionCaught")
fun Context.openURL(url: String?): Boolean =
    try {
        if (url.isNullOrBlank()) {
            Log.e(TAG, "link is null or blank")
            toast(getString(R.string.error_cant_open_url))
            false
        } else {
            startActivity(Intent(ACTION_VIEW, url.toUri()))
            true
        }
    } catch (e: ActivityNotFoundException) {
        Log.e(TAG, "No browser app installed", e)
        toast(getString(R.string.no_browser_app_installed))
        false
    } catch (e: Exception) {
        Log.e(TAG, "Failed to open URL", e)
        toast(getString(R.string.error_cant_open_url))
        false
    }
