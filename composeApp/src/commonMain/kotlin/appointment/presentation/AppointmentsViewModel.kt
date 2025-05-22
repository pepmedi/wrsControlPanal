package appointment.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import appUsers.User
import appointment.domain.AppointmentBookingMaster
import appointment.domain.AppointmentBookingRepository
import core.domain.AppResult
import doctor.domain.DoctorMaster
import doctor.domain.DoctorRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppointmentsViewModel(
    private val repository: AppointmentBookingRepository,
    private val doctorRepository: DoctorRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AppointmentsUiState())
    val uiState = _uiState.asStateFlow()
        .onStart {
            getAllAppointment()
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            _uiState.value
        )

    private suspend fun getAllAppointment() {
        _uiState.update { it.copy(isLoading = true) }
        repository
            .getAllAppointments()
            .collect { result ->
                when (result) {
                    is AppResult.Success -> {
                        val appointments = result.data.toMutableList()
                        val doctorIds = appointments.map { it.doctorId }.distinct()
                        if (doctorIds.isNotEmpty()) {
                            fetchDoctorsDetails(doctorIds, appointments)
                        } else {
                            _uiState.update {
                                it.copy(
                                    appointments = appointments,
                                    isLoading = false
                                )
                            }
                        }
                    }

                    is AppResult.Error -> {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
            }
    }

    private fun fetchDoctorsDetails(
        doctorIds: List<String>,
        appointments: MutableList<AppointmentBookingMaster>
    ) {
        viewModelScope.launch {
            val doctorMap = mutableMapOf<String, DoctorMaster>()

            doctorIds.map { doctorId ->
                async {
                    doctorRepository.getDoctor(doctorId).collect { doctorResult ->
                        if (doctorResult is AppResult.Success) {
                            doctorMap[doctorId] = doctorResult.data
                        }
                    }
                }
            }.awaitAll()
            updateStateWithAppointments(appointments = appointments, doctorMap = doctorMap)
        }
    }

    private fun updateStateWithAppointments(
        appointments: List<AppointmentBookingMaster>,
        doctorMap: Map<String, DoctorMaster>,
        error: String = ""
    ) {
        val allAppointments = appointments.map { appointment ->
            AppointmentDetails(
                appointment = appointment,
                doctor = doctorMap[appointment.doctorId] ?: DoctorMaster()
            )
        }

        _uiState.update { currentState ->
            currentState.copy(
                appointments = appointments.toMutableList(),
                doctorMap = doctorMap,
                allAppointment = allAppointments,
                completedAppointment = allAppointments.filter { it.appointment.status == "0" },
                canceledAppointments = allAppointments.filter { it.appointment.status == "1" },
                waitingAppointments = allAppointments.filter { it.appointment.status == "2" },
                upcomingAppointment = allAppointments.filter { it.appointment.status == "3" },
                isLoading = false,
                error = error
            )
        }
    }

    fun onAction(action: AppointmentScreenAction) {
        when (action) {
            is AppointmentScreenAction.OnStatusChange -> {
                viewModelScope.launch {
                    repository.updateAppointmentStatus(
                        appointmentId = action.appointmentId,
                        status = action.status
                    ).collect { result ->
                        when (result) {
                            is AppResult.Success -> {
                                _uiState.update { currentState ->
                                    val updatedAppointments =
                                        currentState.appointments.map { appointment ->
                                            if (appointment.id == action.appointmentId) {
                                                appointment.copy(status = action.status)
                                            } else {
                                                appointment
                                            }
                                        }
                                            .toMutableList()

//                                    // If the appointment ID doesn't already exist, add it
//                                    val exists = updatedAppointments.any { it.id == action.appointmentId }
//                                    if (!exists) {
//                                        updatedAppointments.add(
//                                            Appointment(
//                                                id = action.appointmentId,
//                                                status = action.status,
//                                                // Fill other required fields here or fetch them if needed
//                                            )
//                                        )
//                                    }

                                    val updatedAllAppointments =
                                        updatedAppointments.map { appointment ->
                                            AppointmentDetails(
                                                appointment = appointment,
                                                doctor = currentState.doctorMap[appointment.doctorId]
                                                    ?: DoctorMaster()
                                            )
                                        }

                                    currentState.copy(
                                        appointments = updatedAppointments.toMutableList(),
                                        allAppointment = updatedAllAppointments,
                                        completedAppointment = updatedAllAppointments.filter { it.appointment.status == "0" }, // appointment completed
                                        canceledAppointments = updatedAllAppointments.filter { it.appointment.status == "1" }, // appointment canceled
                                        waitingAppointments = updatedAllAppointments.filter { it.appointment.status == "2" }, // appointment waiting for confirmation (not confirmed)
                                        upcomingAppointment = updatedAllAppointments.filter { it.appointment.status == "3" }  // appointment confirmed and not completed
                                    )
                                }
                            }

                            is AppResult.Error -> {
                                // You can update an error message in the UI state if needed
                                _uiState.update { it.copy(error = "Failed to update status") }
                            }
                        }
                    }
                }
            }

            is AppointmentScreenAction.OnRefreshClick -> {
                viewModelScope.launch {
                    getAllAppointment()
                }
            }
        }
    }

}

data class AppointmentsUiState(
    val appointments: MutableList<AppointmentBookingMaster> = mutableListOf(),
    val doctorMap: Map<String, DoctorMaster> = emptyMap(),
    val allAppointment: List<AppointmentDetails> = emptyList(),
    val completedAppointment: List<AppointmentDetails> = emptyList(),
    val upcomingAppointment: List<AppointmentDetails> = emptyList(),
    val canceledAppointments: List<AppointmentDetails> = emptyList(),
    val waitingAppointments: List<AppointmentDetails> = emptyList(),
    val isLoading: Boolean = false,
    val error: String = ""
)

data class AppointmentDetails(
    val appointment: AppointmentBookingMaster = AppointmentBookingMaster(),
    val doctor: DoctorMaster = DoctorMaster(),
    val user: User = User()
)

sealed interface AppointmentScreenAction {
    data class OnStatusChange(val appointmentId: String, val status: String) :
        AppointmentScreenAction

    data object OnRefreshClick : AppointmentScreenAction
}