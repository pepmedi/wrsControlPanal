package doctor.domain

import core.domain.DataError

sealed class DoctorSubmissionState {
    data object Idle : DoctorSubmissionState()
    data object Loading : DoctorSubmissionState()
    data class Success(val doctor: DoctorMaster) : DoctorSubmissionState()
    data class Error(val error: DataError.Remote) : DoctorSubmissionState()
}