package doctor.viewModal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import core.domain.AppResult
import doctor.domain.DoctorMaster
import doctor.domain.DoctorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DoctorListViewModel(private val doctorRepository: DoctorRepository) : ViewModel() {

    private val _state = MutableStateFlow(DoctorListUiState())
    val state = _state.asStateFlow()
        .onStart {
            getAllDoctor()
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            _state.value
        )

    fun onAction(action: DoctorListActions) {
        when (action) {
            is DoctorListActions.OnDoctorUpdated -> {
                _state.update {
                    it.copy(doctorList = it.doctorList.map { doctor ->
                        if (doctor.id == action.doctor.id) {
                            action.doctor
                        } else {
                            doctor
                        }
                    })
                }
            }

            is DoctorListActions.OnDoctorAdded -> {
                _state.update {
                    it.copy(doctorList = it.doctorList + action.doctor)
                }
            }

            is DoctorListActions.OnDoctorDateBlocked -> {
                updateBlockDate(action.doctorId, action.blockedDates)
            }

            is DoctorListActions.OnShowUpdateDoctorDate -> {
                _state.update {
                    it.copy(showUpdateDoctorDate = action.show)
                }
            }
        }
    }

    private suspend fun getAllDoctor() {
        _state.update { it.copy(isLoading = true) }
        doctorRepository
            .getAllDoctors()
            .collect { result ->
                when (result) {
                    is AppResult.Success -> {
                        _state.update {
                            it.copy(
                                doctorList = result.data,
                                isLoading = false,
                                error = null
                            )
                        }
                    }

                    is AppResult.Error -> {
                        _state.update {
                            it.copy(error = result.error.name, isLoading = false)
                        }
                    }
                }
            }
    }

    private fun updateBlockDate(doctorId: String, blockedDates: List<String>) {
        _state.update { it.copy(dateBlockUpdating = true) }
        viewModelScope.launch {
            doctorRepository.blockDoctorDates(doctorId = doctorId, blockedDates = blockedDates)
                .collect { result ->
                    when (result) {
                        is AppResult.Success -> {
                            _state.update { currentState ->
                                val updatedDoctors = currentState.doctorList.map { doctor ->
                                    if (doctor.id == doctorId) {
                                        doctor.copy(blockedDates = blockedDates)
                                    } else {
                                        doctor
                                    }
                                }
                                currentState.copy(
                                    dateBlockSuccess = true,
                                    dateBlockUpdating = false,
                                    showUpdateDoctorDate = false,
                                    doctorList = updatedDoctors
                                )
                            }
                        }

                        is AppResult.Error -> {
                            _state.update {
                                it.copy(
                                    dateBlockError = result.error.name,
                                    dateBlockUpdating = false
                                )
                            }
                        }
                    }
                }
        }
    }
}


data class DoctorListUiState(
    val doctorList: List<DoctorMaster> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,

    val dateBlockUpdating: Boolean = false,
    val dateBlockSuccess: Boolean = false,
    val dateBlockError: String? = null,
    val showUpdateDoctorDate: Boolean = false
)

sealed interface DoctorListActions {
    data class OnDoctorUpdated(val doctor: DoctorMaster) : DoctorListActions
    data class OnDoctorAdded(val doctor: DoctorMaster) : DoctorListActions
    data class OnDoctorDateBlocked(val doctorId: String, val blockedDates: List<String>) :
        DoctorListActions

    data class OnShowUpdateDoctorDate(val show: Boolean) : DoctorListActions
}
