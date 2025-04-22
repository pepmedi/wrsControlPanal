package controlPanalUser.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import controlPanalUser.domain.PanelUserCreationAction
import controlPanalUser.domain.PanelUserRepository
import controlPanalUser.domain.PanelUserCreationUiState
import controlPanalUser.domain.UserMasterControlPanel
import core.domain.Result
import doctor.domain.DoctorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import util.getCurrentTimeStamp

class PanelUserCreationViewModel(
    private val panelUserRepository: PanelUserRepository,
    private val doctorRepository: DoctorRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PanelUserCreationUiState())
    val state = _state
        .onStart {
            getAllDoctor()
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            _state.value
        )

    fun onAction(action: (PanelUserCreationAction)) {
        when (action) {
            is PanelUserCreationAction.OnUserRoleChanged -> _state.update {
                it.copy(
                    empType = action.role
                )
            }

            is PanelUserCreationAction.OnUserNameChanged -> _state.update {
                it.copy(userName = action.userName)
            }

            is PanelUserCreationAction.OnUserPassChanged -> _state.update {
                it.copy(
                    userPass = action.userPass
                )
            }

            is PanelUserCreationAction.OnSelectedDoctorChanged -> _state.update {
                it.copy(
                    selectedDoctor = action.doctorsMaster
                )
            }

            is PanelUserCreationAction.OnUserPermissionsChanged -> {
                val permission = action.permission
                val updatedPermissionsMap = _state.value.permissions.toMutableMap()
                val wasChecked = updatedPermissionsMap[permission] ?: false
                val nowChecked = !wasChecked
                updatedPermissionsMap[permission] = nowChecked

                _state.update {
                    it.copy(
                        permissions = updatedPermissionsMap
                    )
                }
            }

            is PanelUserCreationAction.OnCreateUserButtonClicked -> {
                if (_state.value.isFormValid) {
                    registerUser()
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false
                        )
                    }
                }
            }

            is PanelUserCreationAction.OnShowDoctorListClicked -> {
                _state.update { it.copy(showDoctorList = action.clicked) }
            }

            is PanelUserCreationAction.OnIsActiveChanged -> {
                _state.update {
                    it.copy(
                        isActive = action.clicked
                    )
                }
            }

            is PanelUserCreationAction.OnErrorMessageChange -> _state.update { it.copy(isError = "") }

            else -> Unit
        }
    }

    fun resetData() {
        _state.value = PanelUserCreationUiState()
    }

    private fun registerUser() {
        viewModelScope.launch {
            panelUserRepository.createPanelUser(
                userMasterControlPanel = UserMasterControlPanel(
                    userName = _state.value.userName,
                    password = _state.value.userPass,
                    isActive = if (_state.value.isActive) "0" else "1",
                    empType = _state.value.empType,
                    doctorId = _state.value.selectedDoctor.id,
                    permissions = _state.value.permissions.filter { it.value }.keys.toSet()
                        .ifEmpty { setOf("none") },
                    createdAt = getCurrentTimeStamp(),
                    updatedAt = getCurrentTimeStamp()
                )
            )
                .onStart { _state.update { it.copy(isLoading = true) } }
                .onCompletion { _state.update { it.copy(isLoading = false) } }
                .collect { result ->
                    if (result) _state.update { it.copy(isLoading = false, isSuccess = true) }
                }
        }
    }

    private suspend fun getAllDoctor() {
        doctorRepository
            .getAllDoctors()
            .collect { result ->
                when (result) {
                    is Result.Success -> {
                        val doctorList = result.data
                        _state.update { it.copy(doctorList = doctorList) }
                    }

                    is Result.Error -> {
                        _state.update { it.copy(isError = result.error.name) }
                    }
                }
            }
    }
}