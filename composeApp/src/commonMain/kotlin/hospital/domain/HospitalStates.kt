package hospital.domain

import core.domain.DataError


sealed class HospitalStates {
    data object Idle : HospitalStates()
    data object Loading : HospitalStates()
    data object Success : HospitalStates()
    data class Error(val error: DataError.Remote) : HospitalStates()
}