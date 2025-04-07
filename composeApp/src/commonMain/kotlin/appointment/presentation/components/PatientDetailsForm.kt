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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import appointment.presentation.AppointmentDetails
import util.CustomOutlinedText
import util.Util.toMobileFormat
import util.Util.toNameFormat

@Composable
fun PatientDetailsForm(
    state: AppointmentDetails,
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Title
        Text(
            text = "Patient Details",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ToggleChip(
                text = "Yourself",
                isSelected = state.appointment.bookingFor == "0",
                onClick = {

                }
            )
            ToggleChip(
                text = "Another Person",
                isSelected = state.appointment.bookingFor == "1",
                onClick = {

                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Full Name
        Text(text = "Full Name", fontWeight = FontWeight.SemiBold)

        CustomOutlinedText(
            value = state.appointment.patientName.toNameFormat(),
            placeHolder = "Enter Name",
            onValueChange = {  },
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Age
        Text(text = "Age", fontWeight = FontWeight.SemiBold)

        CustomOutlinedText(
            value = state.appointment.age,
            placeHolder = "Enter Age",
            onValueChange = { },
            modifier = Modifier.padding(vertical = 4.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "Mobile No.", fontWeight = FontWeight.SemiBold)

        CustomOutlinedText(
            value = state.appointment.mobileNo.toMobileFormat(),
            placeHolder = "Enter Mobile",
            onValueChange = {  },
            modifier = Modifier.padding(vertical = 4.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Gender
        Text(text = "Gender", fontWeight = FontWeight.SemiBold)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            GenderChip(
                text = "Male",
                isSelected = state.appointment.gender == "Male",
                onClick = {  }
            )

            GenderChip(
                text = "Female",
                isSelected =state.appointment.gender == "Female",
                onClick = {  }
            )

            GenderChip(
                text = "Other",
                isSelected = state.appointment.gender == "Other",
                onClick = {  }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Describe your problem
        Text(
            text = "Describe your problem",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        CustomOutlinedText(
            value = state.appointment.description,
            placeHolder = "Enter Your Problem Here...",
            onValueChange = { },
            maxLine = 5,
            modifier = Modifier.height(120.dp)
        )
    }
}

@Composable
fun GenderChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (isSelected) SecondaryAppColor else Color(0xFFF5F5F5))
            .border(
                border = BorderStroke(1.dp, color = SecondaryAppColor),
                shape = RoundedCornerShape(50.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color.Black,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * A simple composable for toggling between two states (selected/unselected).
 */
@Composable
fun ToggleChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (isSelected) SecondaryAppColor else Color(0xFFF5F5F5))
            .border(
                border = BorderStroke(1.dp, color = SecondaryAppColor),
                shape = RoundedCornerShape(50.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color.Black,
            fontWeight = FontWeight.Medium
        )
    }
}
