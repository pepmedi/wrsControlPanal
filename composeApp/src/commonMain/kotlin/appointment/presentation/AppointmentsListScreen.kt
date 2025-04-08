package appointment.presentation

import BackgroundColors
import PrimaryAppColor
import SecondaryAppColor
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import appointment.domain.AppointmentBookingMaster
import controlPanalUser.domain.UserRole
import controlPanalUser.repository.SessionManager
import doctor.domain.DoctorMaster
import hexToComposeColor
import org.koin.compose.viewmodel.koinViewModel
import util.Util.toNameFormat

@Composable
fun AppointmentsScreenRoot(viewModal: AppointmentsViewModal = koinViewModel()) {

    val uiState by viewModal.uiState.collectAsStateWithLifecycle(initialValue = AppointmentsUiState())
    AppointmentsScreen(
        uiState = uiState
    )
}

@Composable
fun AppointmentsScreen(
    uiState: AppointmentsUiState
) {
    val tabs = listOf("All", "Waiting", "Completed", "Upcoming", "Cancelled")
    var selectedTab by remember { mutableStateOf(0) }

    val currentUser = SessionManager.currentUser
    val isAdmin = currentUser?.role == UserRole.ADMIN
    val linkedDoctorId = currentUser?.linkedDoctorId

    var showDetails by mutableStateOf(false)
    var currentAppointment by mutableStateOf(AppointmentDetails())

    fun filterAppointments(appointment: List<AppointmentDetails>): List<AppointmentDetails> {
        return if (isAdmin) appointment.sortedByDescending { it.appointment.createdAt }
        else appointment.filter { it.appointment.doctorId == linkedDoctorId }
    }

    MaterialTheme {
        Scaffold(containerColor = Color.White) {

            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    CustomTabRow(tabs = tabs, selectedTab = selectedTab) { newTab ->
                        selectedTab = newTab
                    }

                    HorizontalDivider(
                        color = PrimaryAppColor.copy(alpha = 0.8f),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (uiState.isLoading) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                                color = SecondaryAppColor
                            )
                        }
                    } else {
                        val appointmentsToShow = when (selectedTab) {
                            0 -> filterAppointments(uiState.allAppointment)
                            1 -> filterAppointments(uiState.waitingAppointments)
                            2 -> filterAppointments(uiState.completedAppointment)
                            3 -> filterAppointments(uiState.upcomingAppointment)
                            4 -> filterAppointments(uiState.canceledAppointments)
                            else -> filterAppointments(uiState.allAppointment)
                        }
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 400.dp),
                            modifier = Modifier.fillMaxSize().padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(
                                items = appointmentsToShow
                            ) { appointment ->
                                val doctor = appointment.doctor
                                BookingCard(
                                    doctor = doctor,
                                    appointment = appointment.appointment,
                                    onCancel = {},
                                    onReschedule = {},
                                    onCardClick = {
                                        currentAppointment = appointment
                                        showDetails = true
                                    }
                                )
                            }
                        }
                    }
                }
                if (showDetails) {
                    AppointmentDetailsScreenRoot(
                        appointmentDetails = currentAppointment,
                        onBackClick = {
                            showDetails = false
                        })
                }
            }
        }
    }
}

@Composable
fun BookingCard(
    doctor: DoctorMaster,
    appointment: AppointmentBookingMaster,
    onCancel: () -> Unit,
    onReschedule: () -> Unit,
    onCardClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp, vertical = 5.dp)
            .clickable { onCardClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                val backgroundColor =
                    remember { BackgroundColors.random() }
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(backgroundColor),
                    contentAlignment = Alignment.Center
                ) {
//                    AsyncImage(
//                        model = doctor.profilePic,
//                        contentDescription = "Doctor Image",
//                        modifier = Modifier
//                            .size(80.dp),
////                            .clip(CircleShape),
//                        contentScale = ContentScale.Crop
//                    )

                    Icon(imageVector = Icons.Sharp.Person, contentDescription = "user Image")
                }

                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = appointment.patientName.toNameFormat(),
                        color = SecondaryAppColor,
                        fontSize = 20.sp,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = appointment.mobileNo,
                        color = Color.Black,
                        fontSize = 14.sp,
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = appointment.description,
                        color = Color.Black,
                        fontSize = 14.sp,
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Black)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        Icon(
//                            imageVector = Icons.Default.LocationOn,
//                            contentDescription = "Location Icon",
//                            tint = Color.Gray,
//                            modifier = Modifier.size(14.dp)
//                        )
//                        Text(
//                            text = doctor.consltFee,
//                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
//                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Conditionally show buttons based on status
            when (appointment.status) {
                "0" -> { // Completed

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(
                            12.dp,
                            Alignment.CenterHorizontally
                        )
                    ) {
                        OutlinedButton(
                            onClick = {},
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Blue),
                            border = BorderStroke(1.dp, SecondaryAppColor),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Reschedule", color = Color.Black, fontSize = 15.sp)
                        }

                        OutlinedButton(
                            onClick = {},
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Blue),
                            border = BorderStroke(1.dp, SecondaryAppColor),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Add Review", color = Color.Black, fontSize = 15.sp)
                        }
                    }
                }

                "1" -> { /* Canceled - No Buttons */
                }

                "2", "3" -> { // Hold or Upcoming → Show Re-Book
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(
                            12.dp,
                            Alignment.CenterHorizontally
                        )
                    ) {
                        OutlinedButton(
                            onClick = {},
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.Black,
                                containerColor = hexToComposeColor("#E5E7EB")
                            ),
                            border = BorderStroke(1.dp, SecondaryAppColor),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Canceled", color = Color.Black, fontSize = 15.sp)
                        }

                        OutlinedButton(
                            onClick = {},
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Blue),
                            border = BorderStroke(1.dp, SecondaryAppColor),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Reschedule", color = Color.Black, fontSize = 15.sp)
                        }
                    }
                }
            }

        }
    }
}

@Composable
fun CustomTabRow(tabs: List<String>, selectedTab: Int, onTabSelected: (Int) -> Unit) {

    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tabs.forEachIndexed { index, title ->
            Button(
                onClick = { onTabSelected(index) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTab == index) Color(0xFF4A90E2) else Color.White,
                    contentColor = if (selectedTab == index) Color.White else Color.Black
                ),
                border = BorderStroke(2.dp, Color(0xFF4A90E2)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(title, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}