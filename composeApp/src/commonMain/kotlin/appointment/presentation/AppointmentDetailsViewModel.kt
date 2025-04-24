package appointment.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import core.domain.AppResult
import hospital.domain.HospitalMaster
import hospital.domain.HospitalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppointmentDetailsViewModel(private val hospitalRepository: HospitalRepository) :
    ViewModel() {

    private val _state = MutableStateFlow(AppointmentDetailsUiState())
    val state = _state.asStateFlow()
        .onStart {
            getAllHospital()
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            _state.value
        )

    fun updateAppointment(appointmentDetails: AppointmentDetails) {
        _state.update { it.copy(appointmentDetails = appointmentDetails) }
    }

    private fun getAllHospital() {
        viewModelScope.launch {
            hospitalRepository.getAllHospital()
                .collect { result ->
                    when (result) {
                        is AppResult.Success -> {
                            val hospital = result.data
                            val hospitalId = _state.value.appointmentDetails.appointment.hospitalsId
                            val filteredHospital = hospital.filter { it.id in hospitalId }
                            _state.update { it.copy(hospitalList = filteredHospital) }
                        }

                        is AppResult.Error -> {

                        }
                    }
                }
        }
    }
}

data class AppointmentDetailsUiState(
    val appointmentDetails: AppointmentDetails = AppointmentDetails(),
    val hospitalList: List<HospitalMaster> = emptyList()
)