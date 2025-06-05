package achievements.viewmodel

import achievements.data.AchievementMaster
import achievements.data.AchievementsRepository
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
import java.io.File

class AchievementsViewModel(private var achievementsRepository: AchievementsRepository) :
    ViewModel() {

    private val _state = MutableStateFlow(AchievementsUiState())
    val state = _state.asStateFlow()
        .onStart {
            getAllAchievements()
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            AchievementsUiState()
        )

    fun onAction(action: AchievementsActions) {
        when (action) {
            is AchievementsActions.OnAddAchievement -> {
                addAchievement(action.achievementMaster, action.file)
            }

            is AchievementsActions.OnUpdateAchievement -> {
                updateAchievement(action.achievementMaster, action.file)
            }

            is AchievementsActions.OnAchievementAddedSuccessfully -> {
                _state.update { it.copy(achievementAddedSuccessfully = false) }
            }

            is AchievementsActions.OnUpdatedSuccessfully -> {
                _state.update { it.copy(updatedSuccessFully = false) }
            }

            else -> Unit
        }
    }

    private fun getAllAchievements() {
        viewModelScope.launch {
            achievementsRepository.getAllAchievements().collect { result ->
                when (result) {
                    is AppResult.Success -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            achievementsList = result.data
                        )
                    }

                    is AppResult.Error -> {
                        _state.value = _state.value.copy(
                            error = result.error.name,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    private fun addAchievement(achievementMaster: AchievementMaster, file: File) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            achievementsRepository.addAchievement(achievementMaster, file).collect { result ->
                when (result) {
                    is AppResult.Success -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            achievementsList = _state.value.achievementsList + result.data,
                            achievementAddedSuccessfully = true
                        )
                    }

                    is AppResult.Error -> {
                        _state.value = _state.value.copy(
                            error = result.error.name,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    private fun updateAchievement(achievementMaster: AchievementMaster, file: File?) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isUploading = true)
            achievementsRepository.updateAchievement(achievementMaster, file).collect { result ->
                when (result) {
                    is AppResult.Success -> {
                        _state.update {
                            it.copy(
                                achievementsList = it.achievementsList.map { achievement ->
                                    if (achievement.id == achievementMaster.id) {
                                        result.data
                                    } else {
                                        achievement
                                    }
                                },
                                isUploading = false,
                                updatedSuccessFully = true
                            )
                        }
                    }

                    is AppResult.Error -> {
                        _state.update { it.copy(isUploading = false, error = result.error.name) }
                    }
                }
            }
        }
    }

}

data class AchievementsUiState(
    val isLoading: Boolean = false,
    val achievementsList: List<AchievementMaster> = emptyList(),
    val error: String? = null,
    val achievementAddedSuccessfully: Boolean = false,
    val updatedSuccessFully: Boolean = false,
    val isUploading: Boolean = false
)

interface AchievementsActions {
    data class OnAddAchievement(val achievementMaster: AchievementMaster, val file: File) :
        AchievementsActions

    data class OnUpdateAchievement(
        val achievementMaster: AchievementMaster,
        val file: File?
    ) : AchievementsActions

    data object OnAchievementAddedSuccessfully : AchievementsActions
    data object OnUpdatedSuccessfully : AchievementsActions

}