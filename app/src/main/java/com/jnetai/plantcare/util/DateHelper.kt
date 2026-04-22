package com.jnetai.plantcare.util

import java.text.SimpleDateFormat
import java.util.*

object DateHelper {
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.UK)
    private val dateTimeFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.UK)
    private val relativeFormat = SimpleDateFormat("dd MMM", Locale.UK)

    fun format(timestamp: Long): String = dateFormat.format(Date(timestamp))

    fun formatDateTime(timestamp: Long): String = dateTimeFormat.format(Date(timestamp))

    fun formatRelative(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        val days = diff / (24 * 60 * 60 * 1000)
        return when {
            days < 1 -> "Today"
            days == 1L -> "Yesterday"
            days < 7 -> "$days days ago"
            else -> relativeFormat.format(Date(timestamp))
        }
    }

    fun daysUntil(timestamp: Long): Int {
        val now = System.currentTimeMillis()
        val diff = timestamp - now
        return (diff / (24 * 60 * 60 * 1000)).toInt()
    }

    fun nextWaterDate(lastWatered: Long, intervalDays: Int): Long {
        return lastWatered + (intervalDays.toLong() * 24 * 60 * 60 * 1000)
    }

    fun isOverdue(lastWatered: Long, intervalDays: Int): Boolean {
        return System.currentTimeMillis() > nextWaterDate(lastWatered, intervalDays)
    }
}