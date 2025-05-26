package appointment.presentation

import BackgroundColors
import PrimaryAppColor
import SecondaryAppColor
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.sharp.Refresh
import androidx.compose.material.ripple
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import appointment.helpers.AppointmentStatus
import appointment.helpers.AppointmentTab
import component.SlideInScreen
import controlPanalUser.domain.UserRole
import controlPanalUser.repository.SessionManager
import doctor.domain.DoctorMaster
import documents.screen.AllAppointmentRecordsRoot
import hexToComposeColor
import org.koin.compose.viewmodel.koinViewModel
import util.Util.toNameFormat

@Composable
fun AppointmentsScreenRoot(viewModal: AppointmentsViewModel = koinViewModel()) {

    val uiState by viewModal.uiState.collectAsStateWithLifecycle(initialValue = AppointmentsUiState())
    AppointmentsScreen(
        uiState = uiState,
        onAction = { action ->
            viewModal.onAction(action)
        }
    )
}


@Composable
fun AppointmentsScreen(
    uiState: AppointmentsUiState,
    onAction: (AppointmentScreenAction) -> Unit
) {

    var selectedTab by remember { mutableStateOf(AppointmentTab.ALL) }
    var expandedCard by remember { mutableStateOf<AppointmentBookingMaster?>(null) }

    val currentUser = SessionManager.currentUser
    val isAdmin = currentUser?.role == UserRole.ADMIN
    val linkedDoctorId = currentUser?.linkedDoctorId

    var showDetails by mutableStateOf(false)
    var showAddRecords by mutableStateOf(false)
    var currentAppointment by mutableStateOf(AppointmentDetails())

    fun filterAppointments(appointments: List<AppointmentDetails>): List<AppointmentDetails> {
        return if (isAdmin) {
            appointments.sortedByDescending { it.appointment.createdAt }
        } else {
            appointments.filter {
                linkedDoctorId?.contains(it.appointment.doctorId) == true
            }.sortedByDescending { it.appointment.createdAt }
        }
    }

    var showDialog by remember { mutableStateOf(false) }

    MaterialTheme {
        Scaffold(containerColor = Color.White) {

            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Row {
                        IconButton(
                            onClick = {
                                onAction(AppointmentScreenAction.OnRefreshClick)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Sharp.Refresh,
                                contentDescription = ""
                            )
                        }

                        CustomTabRow(
                            tabs = AppointmentTab.entries.toList(),
                            selectedTab = selectedTab,
                            onTabSelected = {
                                selectedTab = it
                            }
                        )
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
                        val appointmentsToShow = when (selectedTab.type) {
                            AppointmentStatus.ALL -> filterAppointments(uiState.allAppointment)
                            AppointmentStatus.WAITING -> filterAppointments(uiState.waitingAppointments)
                            AppointmentStatus.UPCOMING -> filterAppointments(uiState.upcomingAppointment)
                            AppointmentStatus.COMPLETED -> filterAppointments(uiState.completedAppointment)
                            AppointmentStatus.CANCELLED -> filterAppointments(uiState.canceledAppointments)
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
                                    modifier = Modifier.animateItem(),
                                    appointment = appointment.appointment,
                                    onCancel = {
                                        onAction(
                                            AppointmentScreenAction.OnStatusChange(
                                                appointment.appointment.id,
                                                "1"
                                            )
                                        )
                                    },
                                    onConfirm = {
                                        onAction(
                                            AppointmentScreenAction.OnStatusChange(
                                                appointment.appointment.id,
                                                "3"
                                            )
                                        )
                                    },
                                    onCompleted = {
                                        onAction(
                                            AppointmentScreenAction.OnStatusChange(
                                                appointmentId = appointment.appointment.id,
                                                status = "0"
                                            )
                                        )
                                    },
                                    onDetailsClick = {
                                        currentAppointment = appointment
                                        showDetails = true
                                    },
                                    isExpanded = expandedCard == appointment.appointment,
                                    onExpand = { expandedCard = appointment.appointment },
                                    onCollapse = { expandedCard = null },
                                    onUploadRecords = {
                                        showAddRecords = true
                                    }
                                )
                            }
                        }
                    }
                }

                SlideInScreen(showDetails) {
                    AppointmentDetailsScreenRoot(
                        appointmentDetails = currentAppointment,
                        onBackClick = {
                            showDetails = false
                        }
                    )
                }

                SlideInScreen(showAddRecords) {
                    expandedCard?.let { it1 ->
                        AllAppointmentRecordsRoot(
                            appointment = it1,
                            onBackClick = {
                                showAddRecords = false
                            })
                    }
                }
            }
        }
    }
}

@Composable
fun BookingCard(
    doctor: DoctorMaster,
    modifier: Modifier,
    appointment: AppointmentBookingMaster,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    onCompleted: () -> Unit,
    onDetailsClick: () -> Unit,
    isExpanded: Boolean,
    onExpand: () -> Unit,
    onCollapse: () -> Unit,
    onUploadRecords: () -> Unit
) {

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp, vertical = 5.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color.LightGray)
            ) { if (isExpanded) onCollapse() else onExpand() },

        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp).animateContentSize()) {
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

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = doctor.name.toNameFormat(),
                        color = Color.Black,
                        fontSize = 14.sp,
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Black)
                    )

                    Text(
                        text = appointment.description,
                        color = Color.Black,
                        fontSize = 14.sp,
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Black)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {

                    }
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                // Conditionally show buttons based on status
                when (appointment.status) {
                    "0" -> { // Completed

//                        Row(
//                            modifier = Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.spacedBy(
//                                12.dp,
//                                Alignment.CenterHorizontally
//                            )
//                        ) {
//                            OutlinedButton(
//                                onClick = { onConfirm() },
//                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Blue),
//                                border = BorderStroke(1.dp, SecondaryAppColor),
//                                modifier = Modifier.weight(1f)
//                            ) {
//                                Text(text = "Confirm", color = Color.Black, fontSize = 15.sp)
//                            }

//                            OutlinedButton(
//                                onClick = {},
//                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Blue),
//                                border = BorderStroke(1.dp, SecondaryAppColor),
//                                modifier = Modifier.weight(1f)
//                            ) {
//                                Text(text = "Add Review", color = Color.Black, fontSize = 15.sp)
//                            }
//                        }
                    }

                    "1" -> { /* Canceled - No Buttons */
                    }

                    "2" -> { // waiting
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(
                                12.dp,
                                Alignment.CenterHorizontally
                            )
                        ) {
                            OutlinedButton(
                                onClick = { onCancel() },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color.Black,
                                    containerColor = hexToComposeColor("#E5E7EB")
                                ),
                                border = BorderStroke(1.dp, SecondaryAppColor),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "Cancel", color = Color.Black, fontSize = 15.sp)
                            }

                            OutlinedButton(
                                onClick = { onConfirm() },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Blue),
                                border = BorderStroke(1.dp, SecondaryAppColor),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "Confirm", color = Color.Black, fontSize = 15.sp)
                            }
                        }
                    }

                    "3" -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(
                                12.dp,
                                Alignment.CenterHorizontally
                            )
                        ) {
                            OutlinedButton(
                                onClick = { onCancel() },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color.Black,
                                    containerColor = hexToComposeColor("#E5E7EB")
                                ),
                                border = BorderStroke(1.dp, SecondaryAppColor),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "Cancel", color = Color.Black, fontSize = 15.sp)
                            }

                            OutlinedButton(
                                onClick = { onCompleted() },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Blue),
                                border = BorderStroke(1.dp, SecondaryAppColor),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "Completed", color = Color.Black, fontSize = 15.sp)
                            }
                        }
                    }
                }
                // Common "Details" button for all statuses
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    OutlinedButton(
                        onClick = { onDetailsClick() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
                        border = BorderStroke(1.dp, SecondaryAppColor),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Details", fontSize = 15.sp)
                    }

                    OutlinedButton(
                        onClick = { onUploadRecords() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
                        border = BorderStroke(1.dp, SecondaryAppColor),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Upload Records", fontSize = 15.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun CustomTabRow(
    tabs: List<AppointmentTab>, selectedTab: AppointmentTab,
    onTabSelected: (AppointmentTab) -> Unit
) {

    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(5.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        tabs.forEachIndexed { _, tab ->
            Button(
                onClick = { onTabSelected(tab) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTab == tab) Color(0xFF4A90E2) else Color.White,
                    contentColor = if (selectedTab == tab) Color.White else Color.Black
                ),
                border = BorderStroke(2.dp, Color(0xFF4A90E2)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(tab.label, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}