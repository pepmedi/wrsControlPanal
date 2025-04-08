package appointment.presentation.components

import SecondaryAppColor
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hospital.domain.HospitalMaster

@Composable
fun HospitalSection(
    hospitalList: List<HospitalMaster?>,
    columns: Int = 2,
    modifier: Modifier = Modifier,
) {

    Column(modifier = modifier.padding(16.dp)) {
        Text(
            text = "Selected Hospitals",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Splitting the timeSlots list into rows
        val rows = hospitalList.chunked(columns)

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.clip(RoundedCornerShape(10.dp))
                .background(color = SecondaryAppColor.copy(alpha = 0.1f)).padding(10.dp)
        ) {
            rows.forEach { rowItems ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    rowItems.forEach { hospital ->
                        hospital?.let {

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF5F5F5))
                                    .border(
                                        border = BorderStroke(1.dp, SecondaryAppColor),
                                        shape = RoundedCornerShape((12.dp))
                                    )
                                    .clickable {

                                    }
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = hospital.name,
                                    color =  Color.Black,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center,
                                    fontSize = 16.sp
                                )
                            }
                        }

                    }

                    repeat(columns - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}