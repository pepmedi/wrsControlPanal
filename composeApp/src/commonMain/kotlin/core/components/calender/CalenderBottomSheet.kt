package core.components.calender

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.core.now
import com.wearespine.`in`.theme.keylineDimen1
import com.wearespine.`in`.theme.keylineDimen12
import com.wearespine.`in`.theme.keylineDimen16
import com.wearespine.`in`.theme.keylineDimen24
import com.wearespine.`in`.theme.keylineDimen64
import com.wearespine.`in`.theme.keylineDimen8
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import theme.AppColor.Java20
import theme.AppColor.Java500
import theme.AppColor.Vam800
import theme.ButtonTextSmall
import theme.CaptionText
import theme.Gap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalenderBottomSheet(
    onDismiss: () -> Unit,
    preSelectedDate: List<String>,
    onDateSelected: (List<String>) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val scope = rememberCoroutineScope()

    // Maintain selection as mutable state
    var selectedDate by remember {
        mutableStateOf(preSelectedDate.mapNotNull { it.toLongOrNull() }.toMutableSet())
    }

    ModalBottomSheet(
        onDismissRequest = {
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = Java20,
        content = {
            Box(modifier = Modifier.fillMaxSize().padding(keylineDimen16)) {
                Column {

                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "Select Date",
                        style = CaptionText(color = Vam800).copy(textAlign = TextAlign.Start)
                    )

                    Gap(height = keylineDimen16)

                    HorizontalDivider(thickness = keylineDimen1, color = Color.LightGray)

                    Gap(height = keylineDimen8)

                    CalenderCard(
                        selectedDates = selectedDate,
                        onAction = { day ->
                            val millis = day.date.atStartOfDayIn(TimeZone.currentSystemDefault())
                                .toEpochMilliseconds()
                            selectedDate = selectedDate.toMutableSet().also {
                                if (!it.add(millis)) it.remove(millis)
                            }
                        },
                        onMonthClick = {}
                    )

                    Gap(height = keylineDimen16)

                    Row(modifier = Modifier.padding(horizontal = keylineDimen24)) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Calender Card"
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Text(
                            text = "Cancel",
                            style = ButtonTextSmall(color = Java500).copy(textAlign = TextAlign.Center),
                            modifier = Modifier
                                .clip(RoundedCornerShape(keylineDimen8))
                                .clickable(
                                    indication = LocalIndication.current,
                                    interactionSource = remember { MutableInteractionSource() },
                                    onClick = {
                                        scope.launch {
                                            sheetState.hide()
                                            onDismiss()
                                        }
                                    },
                                    role = Role.Button,
                                )
                                .padding(keylineDimen8)
                        )

                        Gap(width = keylineDimen24)

                        Text(
                            text = "Block",
                            style = ButtonTextSmall(color = Java500).copy(textAlign = TextAlign.Center),
                            modifier = Modifier
                                .clip(RoundedCornerShape(keylineDimen8))
                                .clickable(
                                    indication = LocalIndication.current,
                                    interactionSource = remember { MutableInteractionSource() },
                                    onClick = {
                                        scope.launch {
                                            onDateSelected(selectedDate.map { it.toString() })
                                            sheetState.hide()
                                            onDismiss()
                                        }
                                    },
                                    role = Role.Button,
                                    enabled = selectedDate.isNotEmpty()
                                )
                                .padding(keylineDimen8)
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun YearGridPicker(
    selectedYear: Int?,
    currentYear: Int = LocalDate.now().year,
    startYear: Int = 2000,
    endYear: Int = 2100,
    onYearSelected: (Int) -> Unit
) {
    val years = (startYear..endYear).toList()
    val gridState = rememberLazyGridState()

    // Scroll to the selected year when component launches
    LaunchedEffect(Unit) {
        selectedYear?.let {
            val index = years.indexOf(it)
            if (index >= 0) {
                gridState.scrollToItem(index - 1)
            }
        } ?: run {
            val index = years.indexOf(currentYear)
            if (index >= 0) {
                gridState.scrollToItem(index - 1)
            }
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = keylineDimen64),
        state = gridState,
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        items(years) { year ->
            val isSelected = selectedYear == year
            val isCurrent = year == currentYear

            val textColor = when {
                isSelected && isCurrent -> Java20
                isCurrent -> Java500
                isSelected -> Java20
                else -> Color.Unspecified
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .aspectRatio(1f)
                    .testTag("YearItem")
                    .padding(keylineDimen8)
                    .clip(RoundedCornerShape(keylineDimen12))
                    .background(
                        color = if (isSelected) Java500 else Color.Transparent
                    )
                    .border(
                        width = if (isCurrent && !isSelected) 1.5.dp else 0.dp,
                        color = if (isCurrent && !isSelected) Java500 else Color.Transparent,
                        shape = RoundedCornerShape(keylineDimen12)
                    )
                    .clickable { onYearSelected(year) }
            ) {
                Text(
                    text = year.toString(),
                    style = ButtonTextSmall(color = textColor),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}


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
