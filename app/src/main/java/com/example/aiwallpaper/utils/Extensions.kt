package com.example.aiwallpaper.utils

import java.text.SimpleDateFormat
import java.util.*

fun Long.toFormattedDate(): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy  HH:mm", Locale.getDefault())
    return sdf.format(Date(this))
}

fun String.truncated(maxLength: Int = 60): String =
    if (length <= maxLength) this else "${take(maxLength)}…"
