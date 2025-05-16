package services.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import core.domain.AppResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import services.domain.ServicesMaster
import services.domain.ServicesRepository

class AllServicesListViewModel(private val servicesRepository: ServicesRepository) : ViewModel() {

    private val _state = MutableStateFlow(AllServicesListUiState())
    val state = _state.asStateFlow()
        .onStart {
            getAllServices()
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            _state.value
        )

    fun onAction(action: ServicesListActions) {
        when (action) {
            is ServicesListActions.OnServicesAdded -> {
                _state.update { it.copy(servicesList = it.servicesList + action.services) }
            }

            is ServicesListActions.OnServicesUpdated -> {
                _state.update {
                    it.copy(servicesList = it.servicesList.map { services ->
                        if (services.id == action.services.id) action.services else services
                    })
                }
            }
        }

    }

    private fun getAllServices() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            servicesRepository
                .getAllServices()
                .collect { result ->
                    when (result) {
                        is AppResult.Success -> {
                            _state.update { it.copy(servicesList = result.data, isLoading = false) }
                        }

                        is AppResult.Error -> {
                            _state.update { it.copy(error = result.error.name, isLoading = false) }
                        }
                    }
                }
        }
    }

}

data class AllServicesListUiState(
    val servicesList: List<ServicesMaster> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface ServicesListActions {
    data class OnServicesUpdated(val services: ServicesMaster) : ServicesListActions
    data class OnServicesAdded(val services: ServicesMaster) : ServicesListActions
}