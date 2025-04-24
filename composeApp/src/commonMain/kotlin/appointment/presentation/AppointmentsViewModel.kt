package appointment.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import appUsers.User
import appUsers.UserRepository
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
    private val doctorRepository: DoctorRepository,
    private val userRepository: UserRepository
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
                        val userIds = appointments.map { it.userId }.distinct()
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