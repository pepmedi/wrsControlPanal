package services.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import core.domain.onError
import core.domain.onSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import services.domain.ServicesMaster
import services.domain.ServicesRepository
import java.io.File

class UpdateServicesViewModel(private val servicesRepository: ServicesRepository) : ViewModel() {
    private val _state = MutableStateFlow(UpdateServicesUiState())
    val state: StateFlow<UpdateServicesUiState> = _state.asStateFlow()
        .onStart { }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            _state.value
        )

    fun onAction(action: UpdateServicesAction) {
        when (action) {
            is UpdateServicesAction.OnServiceNameChange -> {
                _state.value = _state.value.copy(serviceName = action.serviceName)
            }

            is UpdateServicesAction.OnServiceDescriptionChange -> {
                _state.value = _state.value.copy(serviceDescription = action.serviceDescription)
            }

            is UpdateServicesAction.OnSubmit -> {
                if (_state.value.isFormValid) {
                    updateService()
                }
            }

            is UpdateServicesAction.OnIconChange -> {
                _state.value = _state.value.copy(servicesIconFile = action.file)
            }

            is UpdateServicesAction.OnImageChange -> {
                _state.value = _state.value.copy(servicesImageFile = action.file)
            }

            is UpdateServicesAction.OnServiceReceive -> {
                _state.value = _state.value.copy(
                    services = action.services,
                    serviceName = action.services.name,
                    serviceDescription = action.services.description
                )
            }

            else -> Unit

        }
    }

    private fun updateService() {
        val state = _state.value
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            state.services?.copy(
                name = state.serviceName,
                description = state.serviceDescription
            )?.let {
                servicesRepository.updateService(
                    service = it,
                    iconFile = state.servicesIconFile,
                    imageFile = state.servicesImageFile
                )
                    .collect { result ->
                        result.onSuccess { updatedService ->
                            _state.update { current ->
                                current.copy(
                                    isLoading = false,
                                    isSuccessful = true,
                                    updatedService = updatedService
                                )
                            }
                        }
                        result.onError { error ->
                            _state.value = _state.value.copy(
                                isLoading = false,
                                error = error.name
                            )
                        }
                    }
            }
        }
    }

    fun resetState() {
        _state.value = UpdateServicesUiState()
    }
}

data class UpdateServicesUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val services: ServicesMaster? = null,
    val isSuccessful: Boolean = false,
    val serviceName: String = "",
    val serviceDescription: String = "",
    val servicesIconFile: File? = null,
    val servicesImageFile: File? = null,
    val updatedService: ServicesMaster? = null
) {
    val isFormValid = serviceName.isNotBlank() && serviceDescription.isNotBlank()

    fun getErrorMessage(): String {
        return when {
            serviceName.isBlank() -> "Service Name is required"
            serviceDescription.isBlank() -> "Service Description is required"
            else -> ""
        }
    }
}

sealed interface UpdateServicesAction {
    data class OnServiceNameChange(val serviceName: String) : UpdateServicesAction
    data class OnServiceDescriptionChange(val serviceDescription: String) : UpdateServicesAction
    data object OnSubmit : UpdateServicesAction
    data object OnBackClick : UpdateServicesAction
    data class OnIconChange(val file: File?) : UpdateServicesAction
    data class OnImageChange(val file: File?) : UpdateServicesAction

    data class OnServiceReceive(val services: ServicesMaster) : UpdateServicesAction
}