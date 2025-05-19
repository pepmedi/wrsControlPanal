package controlPanalUser.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import controlPanalUser.domain.PanelUserRepository
import controlPanalUser.domain.UserMasterControlPanel
import core.domain.AppResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PanelUserScreenViewModel(private val panelUserRepository: PanelUserRepository) : ViewModel() {
    private val _state = MutableStateFlow(PanelUserUiState())
    val state = _state.asStateFlow()
        .onStart {
            _state.update { it.copy(isLoading = true) }
            getAllUsers()
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            _state.value
        )

    fun onAction(event: PanelUserUiEvent) {
        when (event) {
            is PanelUserUiEvent.ChangeUserStatus -> {
                updateUserStatus(event.userPanel)
            }

            is PanelUserUiEvent.UserCreated -> {
                event.userPanel?.let { newUser ->
                    _state.update {
                        it.copy(
                            users = it.users + newUser
                        )
                    }
                }
            }

            is PanelUserUiEvent.DeleteUser -> {
                deleteUser(event.userPanel)
            }

            is PanelUserUiEvent.UpdateUser -> {
                event.userPanel?.let { updatedUser ->
                    _state.update {
                        it.copy(users = it.users.map { user ->
                            if (user.id == updatedUser.id) {
                                updatedUser
                            } else {
                                user
                            }
                        })
                    }
                }
            }

            else -> Unit
        }
    }

    private suspend fun getAllUsers() {
        panelUserRepository
            .getAllUser()
            .collect { result ->

                when (result) {
                    is AppResult.Success -> {
                        val users = result.data.toMutableList()
                        _state.update { it.copy(users = users, isLoading = false) }
                    }

                    is AppResult.Error -> {
                        _state.update { it.copy(isLoading = false) }
                    }
                }
            }
    }

    private fun updateUserStatus(userPanel: UserMasterControlPanel) {
        viewModelScope.launch {
            panelUserRepository.updatePanelUser(userPanel)
                .collect { result ->
                    if (result) {
                        _state.update {
                            it.copy(
                                users = it.users.map { user ->
                                    if (user.id == userPanel.id) {
                                        user.copy(isActive = if (userPanel.isActive == "0") "1" else "0")
                                    } else {
                                        user
                                    }
                                }
                            )
                        }
                    }
                }
        }
    }

    private fun deleteUser(userPanel: UserMasterControlPanel) {
        viewModelScope.launch {
            panelUserRepository.deletePanelUser(userPanel.id)
                .collect { result ->
                    if (result) {
                        _state.update {
                            it.copy(
                                users = it.users.filter { user ->
                                    user.id != userPanel.id
                                }
                            )
                        }
                    }
                }
        }
    }

}

data class PanelUserUiState(
    val users: List<UserMasterControlPanel> = emptyList(),
    val isLoading: Boolean = false
)

sealed interface PanelUserUiEvent {
    data class ChangeUserStatus(val userPanel: UserMasterControlPanel) : PanelUserUiEvent
    data class DeleteUser(val userPanel: UserMasterControlPanel) : PanelUserUiEvent
    data class UpdateUser(val userPanel: UserMasterControlPanel?) : PanelUserUiEvent
    data class UserCreated(val userPanel: UserMasterControlPanel?) : PanelUserUiEvent
}