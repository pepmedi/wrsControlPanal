package controlPanalUser.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import controlPanalUser.domain.PanelUserPermissions
import controlPanalUser.domain.PanelUserRepository
import controlPanalUser.domain.UserMasterControlPanel
import core.domain.AppResult
import doctor.domain.DoctorMaster
import doctor.domain.DoctorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import util.getCurrentTimeStamp

class UpdatePanelUserViewModel(
    private val panelUserRepository: PanelUserRepository,
    private val doctorRepository: DoctorRepository
) : ViewModel() {
    private val _state = MutableStateFlow(UpdatePanelUserUiState())
    val state = _state.asStateFlow()
        .onStart {
            getAllDoctor()
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            _state.value
        )

    fun onAction(action: (PanelUserUpdateScreenAction)) {
        when (action) {
            is PanelUserUpdateScreenAction.OnUserRoleChanged -> _state.value = _state.value.copy(
                empType = action.role
            )

            is PanelUserUpdateScreenAction.OnUserNameChanged -> _state.value = _state.value.copy(
                userName = action.userName
            )

            is PanelUserUpdateScreenAction.OnUserPassChanged -> _state.value = _state.value.copy(
                userPass = action.userPass
            )

            is PanelUserUpdateScreenAction.OnSelectedDoctorChanged -> _state.value =
                _state.value.copy(
                    selectedDoctors = action.doctorsMaster
                )

            is PanelUserUpdateScreenAction.OnUserPermissionsChanged -> {
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

            is PanelUserUpdateScreenAction.OnUpdateUserButtonClicked -> {
                if (_state.value.isFormValid) {
                    val currentUser = _state.value.currentUser
                    updateData(
                        userMasterControlPanel = UserMasterControlPanel(
                            id = currentUser.id,
                            userName = _state.value.userName,
                            password = _state.value.userPass,
                            isActive = if (_state.value.isActive) "0" else "1",
                            empType = _state.value.empType,
                            doctorId = _state.value.selectedDoctors.map { it.id },
                            permissions = _state.value.permissions.filter { it.value }.keys
                                .ifEmpty { setOf("none") },
                            createdAt = currentUser.createdAt,
                            updatedAt = getCurrentTimeStamp()
                        )
                    )
                }
            }

            is PanelUserUpdateScreenAction.OnShowDoctorListClicked -> {
                _state.value = _state.value.copy(showDoctorList = action.clicked)
            }

            is PanelUserUpdateScreenAction.OnIsActiveChanged -> {
                _state.value = _state.value.copy(
                    isActive = action.clicked
                )
            }

            is PanelUserUpdateScreenAction.OnErrorMessageChange -> _state.value =
                _state.value.copy(isError = "")

            is PanelUserUpdateScreenAction.OnCurrentUserReceived -> {
                _state.update {
                    it.copy(
                        currentUser = action.userMasterControlPanel,
                        userName = action.userMasterControlPanel.userName,
                        isActive = action.userMasterControlPanel.isActive == "0",
                        empType = action.userMasterControlPanel.empType,
                        userPass = action.userMasterControlPanel.password,
                        selectedDoctors = it.doctorList.filter { doctor ->
                            action.userMasterControlPanel.doctorId.contains(doctor.id)
                        },
                        permissions = action.userMasterControlPanel.permissions.associateWith { true }

                    )
                }
            }

            else -> Unit
        }
    }

    fun resetData() {
        _state.value = UpdatePanelUserUiState()
    }

    private fun updateData(userMasterControlPanel: UserMasterControlPanel) {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            panelUserRepository.updatePanelUser(userMasterControlPanel)
                .collect { result ->
                    if (result) {
                        _state.update {
                            it.copy(
                                isSuccess = true,
                                updatedUser = userMasterControlPanel,
                                isLoading = false
                            )
                        }
                    }
                }
        }
    }

    private suspend fun getAllDoctor() {
        doctorRepository
            .getAllDoctors()
            .collect { result ->
                when (result) {
                    is AppResult.Success -> {
                        val doctorList = result.data

                        val doctorIds = _state.value.currentUser.doctorId

                        val selectedDoctors: List<DoctorMaster> = doctorList.filter { doctor ->
                            doctorIds.contains(doctor.id)
                        }

                        _state.update {
                            it.copy(
                                doctorList = doctorList,
                                selectedDoctors = selectedDoctors
                            )
                        }
                    }

                    is AppResult.Error -> {
                        _state.update { it.copy(isError = result.error.name) }
                    }
                }
            }
    }
}

data class UpdatePanelUserUiState(
    val isActive: Boolean = false,
    val empType: String = "",
    val userPass: String = "",
    val userName: String = "",
    val selectedDoctors: List<DoctorMaster> = emptyList(),
    val doctorList: List<DoctorMaster> = emptyList(),
    val permissions: Map<String, Boolean> = PanelUserPermissions.defaultPermissions,
    val showDoctorList: Boolean = false,
    val isLoading: Boolean = false,
    val isError: String = "",
    val isSuccess: Boolean = false,
    val updatedUser: UserMasterControlPanel? = null,
    val currentUser: UserMasterControlPanel = UserMasterControlPanel()
) {
    val isFormValid: Boolean
        get() = userName.isNotBlank()
                && userPass.isNotBlank()
                && empType.isNotBlank()
                && when (empType) {
            "1" -> selectedDoctors.isNotEmpty() // Doctor needs doctor ID
            "2" -> selectedDoctors.isNotEmpty() && permissions.any { it.value } // Employee needs doctor ID and permissions
            else -> true // Admin
        }

    fun getErrorMessage(): String {
        return when {
            userName.isBlank() -> "Username cannot be empty"
            userPass.isBlank() -> "Password cannot be empty"
            empType.isBlank() -> "Employee type is required"
            empType == "1" && selectedDoctors.isEmpty() -> "Doctor must be linked to a valid doctor ID"
            empType == "2" && selectedDoctors.isEmpty() -> "Employee must be linked to a doctor"
            empType == "2" && permissions.none { it.value } -> "At least one permission must be selected for an employee"
            else -> ""
        }
    }
}

sealed interface PanelUserUpdateScreenAction {
    data class OnUserNameChanged(val userName: String) : PanelUserUpdateScreenAction
    data class OnUserPassChanged(val userPass: String) : PanelUserUpdateScreenAction
    data class OnUserRoleChanged(val role: String) : PanelUserUpdateScreenAction
    data class OnUserPermissionsChanged(val permission: String) : PanelUserUpdateScreenAction
    data class OnSelectedDoctorChanged(val doctorsMaster: List<DoctorMaster>) :
        PanelUserUpdateScreenAction

    data object OnUpdateUserButtonClicked : PanelUserUpdateScreenAction
    data class OnShowDoctorListClicked(val clicked: Boolean) : PanelUserUpdateScreenAction
    data class OnIsActiveChanged(val clicked: Boolean) : PanelUserUpdateScreenAction
    data object OnErrorMessageChange : PanelUserUpdateScreenAction
    data object OnBackButtonClicked : PanelUserUpdateScreenAction
    data class OnCurrentUserReceived(val userMasterControlPanel: UserMasterControlPanel) :
        PanelUserUpdateScreenAction
}
