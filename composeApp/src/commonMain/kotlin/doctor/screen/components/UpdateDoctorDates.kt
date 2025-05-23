package doctor.screen.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.wearespine.`in`.theme.keylineDimen1
import com.wearespine.`in`.theme.keylineDimen16
import com.wearespine.`in`.theme.keylineDimen24
import com.wearespine.`in`.theme.keylineDimen8
import core.components.calender.CalenderCard
import doctor.viewModal.DoctorListActions
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import theme.AppButton
import theme.AppColor.Vam800
import theme.ButtonType
import theme.ButtonViewState
import theme.CaptionText
import theme.Gap

@Composable
fun UpdateDoctorDates(
    doctorId: String,
    preBlockedDates: List<String>,
    onActions: (DoctorListActions) -> Unit,
    isUpdating: Boolean,
    onCloses: () -> Unit,
) {
    // Maintain selection as mutable state
    var selectedDate by remember {
        mutableStateOf(preBlockedDates.mapNotNull { it.toLongOrNull() }.toMutableSet())
    }

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

                Spacer(modifier = Modifier.weight(1f))

                AppButton(
                    onClick = { onCloses() },
                    text = "Cancel",
                    buttonType = ButtonType.EXTRA_SMALL_OUTLINE,
                    modifier = Modifier.padding(keylineDimen8)
                )
//                Text(
//                    text = "Cancel",
//                    style = ButtonTextSmall(color = Java500).copy(textAlign = TextAlign.Center),
//                    modifier = Modifier
//                        .clip(RoundedCornerShape(keylineDimen8))
//                        .clickable(
//                            indication = LocalIndication.current,
//                            interactionSource = remember { MutableInteractionSource() },
//                            onClick = {
//                                onCloses()
//                            },
//                            role = Role.Button,
//                        )
//                        .padding(keylineDimen8)
//                )

                Gap(width = keylineDimen24)

                AppButton(
                    modifier = Modifier.padding(keylineDimen8),
                    onClick = {
                        onActions(
                            DoctorListActions.OnDoctorDateBlocked(
                                doctorId = doctorId,
                                blockedDates = selectedDate.map { it.toString() })
                        )
                    },
                    text = "Block",
                    buttonType = ButtonType.EXTRA_SMALL,
                    viewState = if (isUpdating) ButtonViewState.LOADING else ButtonViewState.DEFAULT,
                    enabled = selectedDate.isNotEmpty()
                )

//                Text(
//                    text = "Block",
//                    style = ButtonTextSmall(color = Java500).copy(textAlign = TextAlign.Center),
//                    modifier = Modifier
//                        .clip(RoundedCornerShape(keylineDimen8))
//                        .clickable(
//                            indication = LocalIndication.current,
//                            interactionSource = remember { MutableInteractionSource() },
//                            onClick = {
//
//
//                            },
//                            role = Role.Button,
//                            enabled = selectedDate.isNotEmpty()
//                        )
//                        .padding(keylineDimen8)
//                )
            }
        }
    }
}