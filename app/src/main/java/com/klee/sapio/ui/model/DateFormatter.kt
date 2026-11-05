package com.klee.sapio.ui.model

import android.content.res.Resources
import com.klee.sapio.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

private const val ISO_DATE_PATTERN = "yyyy-MM-dd"
private const val DAYS_PER_MONTH = 30
private const val DAYS_PER_YEAR = 365

fun absoluteDate(date: Date?): String {
    if (date == null) {
        return ""
    }

    val format = SimpleDateFormat(ISO_DATE_PATTERN, Locale.ROOT)
    format.timeZone = TimeZone.getTimeZone("UTC")

    return format.format(date)
}

fun relativeDate(date: Date?, resources: Resources): String {
    if (date == null) {
        return ""
    }

    val diffMs = System.currentTimeMillis() - date.time
    val diffMinutes = TimeUnit.MILLISECONDS.toMinutes(diffMs).toInt()
    val diffHours = TimeUnit.MILLISECONDS.toHours(diffMs).toInt()
    val diffDays = TimeUnit.MILLISECONDS.toDays(diffMs).toInt()
    val diffMonths = diffDays / DAYS_PER_MONTH
    val diffYears = diffDays / DAYS_PER_YEAR

    return when {
        diffYears >= 1 -> resources.getQuantityString(R.plurals.date_years_ago, diffYears, diffYears)
        diffMonths >= 1 -> resources.getQuantityString(R.plurals.date_months_ago, diffMonths, diffMonths)
        diffDays >= 1 -> resources.getQuantityString(R.plurals.date_days_ago, diffDays, diffDays)
        diffHours >= 1 -> resources.getQuantityString(R.plurals.date_hours_ago, diffHours, diffHours)
        else -> resources.getQuantityString(R.plurals.date_minutes_ago, diffMinutes, diffMinutes)
    }
}
