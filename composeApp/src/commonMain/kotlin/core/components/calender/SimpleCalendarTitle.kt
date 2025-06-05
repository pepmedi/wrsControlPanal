package core.components.calender

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.core.YearMonth
import com.kizitonwose.calendar.core.minusMonths
import com.wearespine.`in`.theme.keylineDimen4
import com.wearespine.`in`.theme.keylineDimen6
import com.wearespine.`in`.theme.keylineDimen8
import theme.AppColor.Vam800
import theme.ButtonTextSmall
import theme.Gap


@Composable
fun SimpleCalendarTitle(
    modifier: Modifier,
    currentMonth: YearMonth,
    isYearPickerVisible: Boolean,
    onMonthClick: () -> Unit,
    isHorizontal: Boolean = true,
    goToPrevious: () -> Unit,
    goToNext: () -> Unit,
) {
    val today = YearMonth.now()
    val isPreviousDisabled = currentMonth.minusMonths(1) < today

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Start - Left Arrow
        if (!isYearPickerVisible) {
            CalendarNavigationIcon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Previous",
                onClick = { if (!isPreviousDisabled) goToPrevious() },
                iconTint = if (isPreviousDisabled) Color.Gray else Vam800,
                isHorizontal = isHorizontal,
            )
        }


        // Center - Month Text + Up/Down Arrows
        Box(
            modifier = Modifier
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(keylineDimen8))
                    .clickable(
                        indication = LocalIndication.current,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = { onMonthClick() }
                    )
                    .padding(horizontal = keylineDimen6, vertical = keylineDimen4),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier
                        .testTag("MonthTitle"),
                    text = currentMonth.displayText(),
                    style = ButtonTextSmall(color = Vam800).copy(textAlign = TextAlign.Center)
                )

                Gap(width = keylineDimen6)

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Up",
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(90f)
                )
            }
        }

        if (!isYearPickerVisible) {
            // End - Right Arrow
            CalendarNavigationIcon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next",
                onClick = goToNext,
                isHorizontal = isHorizontal,
            )
        }
    }
}

@Composable
private fun CalendarNavigationIcon(
    imageVector: ImageVector,
    contentDescription: String,
    isHorizontal: Boolean = true,
    iconTint: Color = Vam800,
    onClick: () -> Unit,
) = Box(
    modifier = Modifier
        .fillMaxHeight()
        .aspectRatio(1f)
        .clip(RoundedCornerShape(keylineDimen8))
        .clickable(
            indication = LocalIndication.current,
            interactionSource = remember { MutableInteractionSource() },
            role = Role.Button,
            onClick = onClick
        ),
) {
    val rotation by animateFloatAsState(if (isHorizontal) 0f else 90f)
    Icon(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp)
            .align(Alignment.Center)
            .rotate(rotation),
        imageVector = imageVector,
        contentDescription = contentDescription,
        tint = iconTint
    )
}
