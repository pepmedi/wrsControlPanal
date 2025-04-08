package appointment.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import appointment.presentation.components.HospitalSection
import appointment.presentation.components.PatientDetailsForm
import core.CancelButton
import org.koin.compose.viewmodel.koinViewModel
import util.Util.toNameFormat

@Composable
fun AppointmentDetailsScreenRoot(
    viewModal: AppointmentDetailsViewModal = koinViewModel(),
    appointmentDetails: AppointmentDetails,
    onBackClick: () -> Unit
) {

    val uiState by viewModal.state.collectAsStateWithLifecycle()
    viewModal.updateAppointment(appointmentDetails)

    AppointmentDetailsScreen(uiState,
        onBackClick = {
            onBackClick()
        })

}

@Composable
fun AppointmentDetailsScreen(
    state: AppointmentDetailsUiState,
    onBackClick: () -> Unit
) {
    MaterialTheme {
        Scaffold(containerColor = Color.White) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

                    Text(
                        text = state.appointmentDetails.doctor.name.toNameFormat(),
                        modifier = Modifier.padding(16.dp)
                    )
                    HospitalSection(state.hospitalList)
                    PatientDetailsForm(state.appointmentDetails)
                    CancelButton(onBackClick = onBackClick, modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}

