package hospital.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import core.domain.Result
import hospital.domain.HospitalMaster
import hospital.domain.HospitalRepository
import hospital.domain.HospitalStates
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HospitalViewModal(private val hospitalRepository: HospitalRepository):ViewModel() {
    private val _hospitalStates = MutableStateFlow<HospitalStates>(
        HospitalStates.Idle)
    val hospitalStates: StateFlow<HospitalStates> get() = _hospitalStates

    fun addHospital(hospitalMaster: HospitalMaster){
        viewModelScope.launch {
            _hospitalStates.value = HospitalStates.Loading
            hospitalRepository.addHospitalToDatabase(hospitalMaster)
                .collect{ result->
                    _hospitalStates.value = when (result){
                        is Result.Success -> HospitalStates.Success
                        is Result.Error -> HospitalStates.Error(result.error)
                        else -> HospitalStates.Idle
                    }
                }
        }
    }

    fun resetSubmissionState() {
        _hospitalStates.value = HospitalStates.Idle
    }
}