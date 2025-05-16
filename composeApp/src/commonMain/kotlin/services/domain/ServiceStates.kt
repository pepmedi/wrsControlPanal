package services.domain

import core.domain.DataError

sealed class ServiceStates {
    data object Idle : ServiceStates()
    data object Loading : ServiceStates()
    data class Success(val addedService: ServicesMaster? = null) : ServiceStates()
    data class Error(val error: DataError.Remote) : ServiceStates()
}