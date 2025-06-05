package util

import androidx.compose.ui.text.intl.Locale
import core.components.calender.getDisplayName
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun String.toFormattedDate(): String {
    return try {
        val millis = this.toLong()
        val instant = Instant.fromEpochMilliseconds(millis)
        val timeZone = TimeZone.currentSystemDefault()
        val localDate = instant.toLocalDateTime(timeZone).date

        val day = localDate.dayOfMonth
        val month = localDate.month.getDisplayName(short = false, locale = Locale.current)
        val year = localDate.year

        "$day $month $year"
    } catch (e: Exception) {
        "" // or return "Invalid date"
    }
}