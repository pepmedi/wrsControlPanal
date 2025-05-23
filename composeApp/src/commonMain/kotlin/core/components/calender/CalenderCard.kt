package core.components.calender

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.YearMonth
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.minusMonths
import com.kizitonwose.calendar.core.now
import com.kizitonwose.calendar.core.plusMonths
import com.wearespine.`in`.theme.keylineDimen12
import com.wearespine.`in`.theme.keylineDimen8
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import theme.AppColor.Java20
import theme.AppColor.Java500
import theme.AppColor.Vam800
import theme.ButtonTextSmall
import java.time.ZoneId

@Composable
fun CalenderCard(
    adjacentMonths: Int = 24,
    selectedYearMonth: YearMonth = YearMonth.now(),
    onMonthClick: (YearMonth) -> Unit,
    selectedDates: Set<Long>,
    onAction: (CalendarDay) -> Unit
) {
    var isYearPickerVisible by remember { mutableStateOf(false) }
    var selectedMonth by remember { mutableStateOf(selectedYearMonth) }

    val today = remember { LocalDate.now() }
    val daysOfWeek = remember { daysOfWeek() }
    val coroutineScope = rememberCoroutineScope()

    // Calculate calendar bounds based on selected month
    val startMonth = selectedMonth.minusMonths(adjacentMonths)
    val endMonth = selectedMonth.plusMonths(adjacentMonths)

    // Trigger full recomposition of calendar state on selectedMonth change
    key(selectedMonth) {
        val state = rememberCalendarState(
            startMonth = startMonth,
            endMonth = endMonth,
            firstVisibleMonth = selectedMonth,
            firstDayOfWeek = daysOfWeek.first(),
        )

        val visibleMonth = rememberFirstMostVisibleMonth(state, viewportPercent = 90f)

        Column(modifier = Modifier.fillMaxWidth()) {

            SimpleCalendarTitle(
                modifier = Modifier.padding(horizontal = 8.dp),
                currentMonth = visibleMonth.yearMonth,
                isYearPickerVisible = isYearPickerVisible,
                onMonthClick = {
                    onMonthClick(visibleMonth.yearMonth)
                    isYearPickerVisible = !isYearPickerVisible
                },
                goToPrevious = {
                    coroutineScope.launch {
                        val previous = state.firstVisibleMonth.yearMonth.previous
                        selectedMonth = previous
                    }
                },
                goToNext = {
                    coroutineScope.launch {
                        val next = state.firstVisibleMonth.yearMonth.next
                        selectedMonth = next
                    }
                },
            )

            // Year Picker (no animation needed)
            AnimatedVisibility(visible = isYearPickerVisible) {
                YearGridPicker(
                    selectedYear = selectedMonth.year,
                    currentYear = today.year,
                    onYearSelected = { year ->
                        val newMonth = YearMonth(year, selectedMonth.month)
                        selectedMonth = newMonth
                        isYearPickerVisible = false
                    }
                )
            }

            // Calendar with fadeIn/fadeOut animation
            AnimatedVisibility(
                visible = !isYearPickerVisible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                HorizontalCalendar(
                    modifier = Modifier.testTag("Calendar"),
                    state = state,
                    dayContent = { day ->
                        if (day.position == DayPosition.MonthDate) {
                            val millis = day.date.atStartOfDayIn(TimeZone.currentSystemDefault())
                                .toEpochMilliseconds()
                            Day(
                                day = day,
                                today = today,
                                isSelected = selectedDates.contains(millis)
                            ) {
                                onAction(day)
                            }
                        }
                    },
                    monthHeader = {
                        MonthHeader(daysOfWeek = daysOfWeek)
                    },
                    monthBody = { _, content ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
                        ) {
                            content()
                        }
                    }
                )
            }
        }
    }
}


@Composable
private fun MonthHeader(daysOfWeek: List<DayOfWeek>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("MonthHeader"),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        for (dayOfWeek in daysOfWeek) {
            Text(
                modifier = Modifier
                    .padding(keylineDimen8),
                style = ButtonTextSmall(color = Vam800).copy(textAlign = TextAlign.Center),
                text = dayOfWeek.displayText().dropLast(1),
            )
        }
    }
}

@Composable
private fun Day(
    day: CalendarDay,
    today: LocalDate,
//    isDisabled: Boolean,
    isSelected: Boolean,
    onClick: (CalendarDay) -> Unit
) {
    val isToday = day.date == today
    val isSunday =
        day.date.dayOfWeek == DayOfWeek.SUNDAY
    val shouldDisable = isSunday //isDisabled
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .testTag("MonthDay")
            .padding(keylineDimen8)
            .clip(RoundedCornerShape(keylineDimen12))
            .background(color = if (isSelected) Java500 else Color.Transparent)
            .border(
                width = if (isToday) 1.5.dp else 0.dp,
                color = if (isToday) Java500 else Color.Transparent,
                shape = RoundedCornerShape(keylineDimen12)
            )
            .clickable(
                enabled = day.position == DayPosition.MonthDate && !shouldDisable,
                showRipple = !isSelected,
                onClick = { onClick(day) },
            ),
        contentAlignment = Alignment.Center,
    ) {
        val textColor = when {
            isSelected && isToday -> Java20
            isToday -> Java500
            isSelected -> Java20
//            isDisabled -> Color.Gray
            day.position == DayPosition.InDate || day.position == DayPosition.OutDate -> Color.Gray
            else -> Color.Unspecified
        }
        Text(
            text = day.date.dayOfMonth.toString(),
            style = ButtonTextSmall(color = textColor)//if (isDisabled) Color.Gray else textColor
        )
    }
}