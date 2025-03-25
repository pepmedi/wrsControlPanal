package doctor.domain

import core.domain.DataError

sealed class DoctorSubmissionState {
    data object Idle : DoctorSubmissionState()
    data object Loading : DoctorSubmissionState()
    data object Success : DoctorSubmissionState()
    data class Error(val error: DataError.Remote) : DoctorSubmissionState()
}