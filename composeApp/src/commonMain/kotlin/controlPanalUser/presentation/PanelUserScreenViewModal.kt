package controlPanalUser.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import controlPanalUser.domain.PanelUserRepository
import controlPanalUser.domain.UserMasterControlPanel
import core.domain.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class PanelUserScreenViewModal(private val panelUserRepository: PanelUserRepository) : ViewModel() {
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

    private suspend fun getAllUsers() {
        panelUserRepository
            .getAllUser()
            .collect { result ->

                when (result) {
                    is Result.Success -> {
                        val appointments = result.data.toMutableList()
                        _state.update { it.copy(users = appointments, isLoading = false) }
                    }

                    is Result.Error -> {
                        _state.update { it.copy(isLoading = false) }
                    }
                }
            }
    }

}

data class PanelUserUiState(
    val users: List<UserMasterControlPanel> = emptyList(),
    val isLoading: Boolean = false
)