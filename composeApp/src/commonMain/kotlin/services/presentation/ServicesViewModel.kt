package services.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import core.domain.AppResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import services.domain.ServiceStates
import services.domain.ServicesMaster
import services.domain.ServicesRepository
import java.io.File

class ServicesViewModel(private val servicesRepository: ServicesRepository) : ViewModel() {
    private val _serviceStates = MutableStateFlow<ServiceStates>(
        ServiceStates.Idle
    )
    val serviceStates: StateFlow<ServiceStates> get() = _serviceStates

    fun addService(service: ServicesMaster, imageFile: File, iconFile: File) {
        viewModelScope.launch {
            _serviceStates.value = ServiceStates.Loading
            servicesRepository.addServiceToDatabase(
                service = service,
                imageFile = imageFile,
                iconFile = iconFile
            )
                .collect { result ->
                    _serviceStates.value = when (result) {
                        is AppResult.Success -> ServiceStates.Success
                        is AppResult.Error -> ServiceStates.Error(result.error)
                        else -> ServiceStates.Idle
                    }
                }
        }
    }

    fun resetState() {
        _serviceStates.value = ServiceStates.Idle
    }
}