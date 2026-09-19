package com.klee.sapio.ui.model

import android.content.res.Resources
import com.klee.sapio.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private const val ISO_DATE_PATTERN = "yyyy-MM-dd"

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
    val diffMinutes = (diffMs / (1000 * 60)).toInt()
    val diffHours = (diffMs / (1000 * 60 * 60)).toInt()
    val diffDays = (diffMs / (1000 * 60 * 60 * 24)).toInt()
    val diffMonths = diffDays / 30
    val diffYears = diffDays / 365

    return when {
        diffYears >= 1 -> resources.getQuantityString(R.plurals.date_years_ago, diffYears, diffYears)
        diffMonths >= 1 -> resources.getQuantityString(R.plurals.date_months_ago, diffMonths, diffMonths)
        diffDays >= 1 -> resources.getQuantityString(R.plurals.date_days_ago, diffDays, diffDays)
        diffHours >= 1 -> resources.getQuantityString(R.plurals.date_hours_ago, diffHours, diffHours)
        else -> resources.getQuantityString(R.plurals.date_minutes_ago, diffMinutes, diffMinutes)
    }
}
