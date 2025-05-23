package doctor.screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wearespine.`in`.theme.keylineDimen16
import com.wearespine.`in`.theme.keylineDimen8
import core.components.calender.CalenderCard
import doctor.domain.DoctorMaster
import doctor.viewModal.DoctorListActions
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import theme.AppButton
import theme.AppColor.Java20
import theme.AppColor.Java500
import theme.AppTheme
import theme.ButtonType
import theme.ButtonViewState
import theme.Gap

@Composable
fun UpdateDoctorDatesScreen(
    doctor: DoctorMaster,
    preBlockedDates: List<String>,
    onActions: (DoctorListActions) -> Unit,
    isUpdating: Boolean,
    onCloses: () -> Unit,
) {
    var selectedDate by remember {
        mutableStateOf(preBlockedDates.mapNotNull { it.toLongOrNull() }.toMutableSet())
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(keylineDimen16),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(keylineDimen16),
            colors = CardDefaults.cardColors(containerColor = Java20),
            elevation = CardDefaults.cardElevation(keylineDimen8),
            modifier = Modifier
                .widthIn(max = 500.dp)
                .heightIn(max = 600.dp)
                .padding(keylineDimen16)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(keylineDimen16)
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Select Date to block for ${doctor.name}",
                    style = AppTheme.typography.subtitleBold.copy(
                        color = Java500,
                        textAlign = TextAlign.Center
                    )
                )

                Gap(height = keylineDimen16)
                HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
                Gap(height = keylineDimen8)

                val todayStartMillis = Clock.System.now()
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .date.atStartOfDayIn(TimeZone.currentSystemDefault())
                    .toEpochMilliseconds()

                // Filter out past dates before assigning
                val futureSelectedDates =
                    selectedDate.filter { it >= todayStartMillis }.toMutableSet()

                CalenderCard(
                    selectedDates = futureSelectedDates,
                    onAction = { day ->
                        val millis = day.date.atStartOfDayIn(TimeZone.currentSystemDefault())
                            .toEpochMilliseconds()

                        if (millis >= todayStartMillis) {
                            selectedDate = futureSelectedDates.toMutableSet().also {
                                if (!it.add(millis)) it.remove(millis)
                            }
                        }
                    },
                )

                Spacer(modifier = Modifier.weight(1f))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = keylineDimen16),
                    horizontalArrangement = Arrangement.End
                ) {
                    AppButton(
                        onClick = onCloses,
                        text = "Cancel",
                        buttonType = ButtonType.EXTRA_SMALL_OUTLINE,
                        modifier = Modifier.padding(end = keylineDimen8)
                    )

                    AppButton(
                        onClick = {
                            onActions(
                                DoctorListActions.OnDoctorDateBlocked(
                                    doctorId = doctor.id,
                                    blockedDates = selectedDate.map { it.toString() }
                                )
                            )
                        },
                        text = "Block",
                        buttonType = ButtonType.EXTRA_SMALL,
                        viewState = if (isUpdating) ButtonViewState.LOADING else ButtonViewState.DEFAULT,
                        enabled = selectedDate.isNotEmpty()
                    )
                }
            }
        }
    }
}
