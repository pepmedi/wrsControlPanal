package core.components.calender

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.core.now
import com.wearespine.`in`.theme.keylineDimen12
import com.wearespine.`in`.theme.keylineDimen64
import com.wearespine.`in`.theme.keylineDimen8
import kotlinx.datetime.LocalDate
import theme.AppColor.Java20
import theme.AppColor.Java500
import theme.ButtonTextSmall

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