package services.domain

import core.domain.DataError

sealed class ServiceStates {
    data object Idle : ServiceStates()
    data object Loading : ServiceStates()
    data object Success : ServiceStates()
    data class Error(val error: DataError.Remote) : ServiceStates()
}