package core.components.calender

import androidx.compose.ui.text.intl.Locale
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Month
import java.time.format.TextStyle

actual fun Month.getDisplayName(short: Boolean, locale: Locale): String {
    val style = if (short) TextStyle.SHORT else TextStyle.FULL
    return this.getDisplayName(style, java.util.Locale.forLanguageTag(locale.toLanguageTag()))
}

actual fun DayOfWeek.getDisplayName(narrow: Boolean, locale: Locale): String {
    val style = if (narrow) TextStyle.NARROW else TextStyle.SHORT
    return this.getDisplayName(style, java.util.Locale.forLanguageTag(locale.toLanguageTag()))
}
