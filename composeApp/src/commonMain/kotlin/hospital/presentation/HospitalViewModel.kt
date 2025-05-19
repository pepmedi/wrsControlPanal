package hospital.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import core.domain.AppResult
import hospital.domain.HospitalMaster
import hospital.domain.HospitalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HospitalViewModel(private val hospitalRepository: HospitalRepository) : ViewModel() {

    private val _uiStates = MutableStateFlow(HospitalUiState())
    val uiState = _uiStates.asStateFlow()
        .onStart {
            getAllHospitals()
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            _uiStates.value
        )

    fun onAction(action: HospitalActions) {
        when (action) {
            is HospitalActions.OnAddHospital -> {
                addHospital(action.hospitalMaster)
            }

            is HospitalActions.OnUpdateHospital -> {
                updateHospital(action.hospitalMaster)
            }

            is HospitalActions.OnHospitalAddedSuccessfully -> {
                _uiStates.update { it.copy(hospitalAddedSuccessfully = false) }
            }

            is HospitalActions.OnUpdatedSuccessfully -> {
                _uiStates.update { it.copy(updatedSuccessFully = false) }
            }

            is HospitalActions.OnDeleteHospital -> {
                deleteHospital(action.hospitalMaster)
            }

            else -> Unit
        }
    }

    private fun addHospital(hospitalMaster: HospitalMaster) {
        viewModelScope.launch {
            _uiStates.update { it.copy(isUploading = true) }
            hospitalRepository.addHospitalToDatabase(hospitalMaster)
                .collect { result ->
                    when (result) {
                        is AppResult.Success -> {
                            _uiStates.update {
                                it.copy(
                                    hospitalAddedSuccessfully = true,
                                    hospitalsList = it.hospitalsList + result.data,
                                    isUploading = false
                                )
                            }
                        }

                        is AppResult.Error -> {
                            _uiStates.update {
                                it.copy(
                                    error = result.error.name,
                                    isUploading = false
                                )
                            }
                        }
                    }
                }
        }
    }

    private fun getAllHospitals() {
        viewModelScope.launch {
            _uiStates.update { it.copy(isLoading = true) }
            hospitalRepository
                .getAllHospital()
                .collect { result ->
                    when (result) {
                        is AppResult.Success -> {
                            _uiStates.update {
                                it.copy(
                                    hospitalsList = result.data,
                                    isLoading = false
                                )
                            }
                        }

                        is AppResult.Error -> {
                            _uiStates.update {
                                it.copy(
                                    error = result.error.name,
                                    isLoading = false
                                )
                            }
                        }
                    }
                }
        }
    }

    private fun updateHospital(hospitalMaster: HospitalMaster) {
        viewModelScope.launch {
            _uiStates.update { it.copy(isUploading = true) }
            hospitalRepository
                .updateHospital(hospitalMaster)
                .collect { result ->
                    when (result) {
                        is AppResult.Success -> {
                            _uiStates.update {
                                it.copy(hospitalsList = it.hospitalsList.map { hospital ->
                                    if (hospital.id == hospitalMaster.id) {
                                        hospitalMaster
                                    } else {
                                        hospital
                                    }
                                }, isUploading = false, updatedSuccessFully = true)
                            }
                        }

                        is AppResult.Error -> {
                            _uiStates.update { it.copy(error = result.error.name) }
                        }
                    }
                }
        }
    }

    private fun deleteHospital(hospital: HospitalMaster) {
        println("🔄 Starting deleteHospital for: ${hospital.id} - ${hospital.name}")

        viewModelScope.launch {
            hospitalRepository.deleteHospital(hospital).collect { result ->
                when (result) {
                    is AppResult.Success -> {
                        println("✅ Hospital deleted successfully from repository: ${hospital.id}")

                        _uiStates.update {
                            val newList = it.hospitalsList.filter { item ->
                                val match = item.id == hospital.id
                                println("🧹 Filtering item: ${item.name} - match: $match")
                                !match
                            }

                            println("📦 Updated hospitals list size: ${newList.size}")
                            it.copy(hospitalsList = newList, isUploading = false)
                        }
                    }

                    is AppResult.Error -> {
                        println("❌ Error deleting hospital: ${result.error.name}")
                        _uiStates.update { it.copy(error = result.error.name) }
                    }
                }
            }
        }
    }
}

data class HospitalUiState(
    val hospitalsList: List<HospitalMaster> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val hospitalAddedSuccessfully: Boolean = false,
    val updatedSuccessFully: Boolean = false,
    val isUploading: Boolean = false
)

sealed interface HospitalActions {
    data class OnAddHospital(val hospitalMaster: HospitalMaster) : HospitalActions
    data class OnUpdateHospital(val hospitalMaster: HospitalMaster) : HospitalActions
    data object OnHospitalAddedSuccessfully : HospitalActions
    data object OnUpdatedSuccessfully : HospitalActions
    data class OnDeleteHospital(val hospitalMaster: HospitalMaster) : HospitalActions

}