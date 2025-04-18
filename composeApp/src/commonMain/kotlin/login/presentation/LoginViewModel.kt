package login.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import controlPanalUser.domain.UserMasterControlPanel
import controlPanalUser.domain.UserRole
import controlPanalUser.domain.UserSession
import controlPanalUser.repository.SessionManager
import core.domain.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import login.domain.LoginRepository

class LoginViewModel(
    private val loginRepository: LoginRepository
) : ViewModel() {

    private var _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun onUsernameChange(value: String) {
        _uiState.update { it.copy(username = value) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value) }
    }
    fun onErrorMessageChange(){
        _uiState.update { it.copy(errorMessage = "") }
    }

    fun resetLoginUiState(): LoginUiState {
        return LoginUiState()
    }
    fun login() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true, errorMessage = null
                )
            }
            val result =
                loginRepository.isValidUser(_uiState.value.username, _uiState.value.password)

            when (result) {
                is Result.Success -> {
                    val user = result.data
                    if (user.isActive == "0") {
                        val role = when (user.empType) {
                            "0" -> UserRole.ADMIN
                            "1" -> UserRole.DOCTOR
                            "2" -> UserRole.EMPLOYEE
                            else -> UserRole.EMPLOYEE
                        }

                        SessionManager.currentUser = UserSession(
                            id = user.id,
                            userName = user.userName,
                            role = role,
                            permissions = user.permissions.toSet(),
                            linkedDoctorId = if (role == UserRole.EMPLOYEE || role == UserRole.DOCTOR) user.doctorId else null
                        )

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                loginSuccess = true,
                                user = user,
                                role = role
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "User ID is not Active"
                            )
                        }
                    }
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "User not found"
                        )
                    }
                }

            }
        }
    }
}

data class LoginUiState(
    val username: String = "rajeev",
    val password: String = "pass",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loginSuccess: Boolean = false,
    val user: UserMasterControlPanel? = null,
    val role: UserRole? = null
)
