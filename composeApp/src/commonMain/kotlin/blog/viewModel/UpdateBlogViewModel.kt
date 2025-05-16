package blog.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import blog.domain.BlogMaster
import blog.domain.BlogRepository
import core.domain.AppResult
import doctor.domain.DoctorMaster
import doctor.domain.DoctorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import util.getCurrentTimeStamp
import java.io.File

class UpdateBlogViewModel(
    private val blogRepository: BlogRepository,
    private val doctorRepository: DoctorRepository
) : ViewModel() {
    private val _state = MutableStateFlow(UpdateBlogUiState())
    val state: StateFlow<UpdateBlogUiState> = _state.asStateFlow()
        .onStart {
            getDoctorList()
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            _state.value
        )

    fun onAction(action: UpdateBlogAction) {
        when (action) {
            is UpdateBlogAction.OnBlogTitleChange -> {
                _state.value = _state.value.copy(title = action.title)
            }

            is UpdateBlogAction.OnBlogDescriptionChange -> {
                _state.value = _state.value.copy(blogDescription = action.description)
            }

            is UpdateBlogAction.OnDoctorChange -> {
                _state.value = _state.value.copy(doctor = action.doctor)
            }

            is UpdateBlogAction.OnImageChange -> {
                _state.value = _state.value.copy(imageFile = action.file)
            }

            is UpdateBlogAction.OnShowDoctorListClicked -> {
                _state.value = _state.value.copy(showDoctorList = action.clicked)
            }

            is UpdateBlogAction.OnSubmit -> {
                if (_state.value.isFormValid) {
                    updateBlog()
                }
            }

            is UpdateBlogAction.OnBlogReceive -> {
                _state.value = _state.value.copy(
                    blogDetails = action.blogDetails,
                    title = action.blogDetails.title,
                    blogDescription = action.blogDetails.description
                )
            }

            else -> Unit
        }
    }

    private fun getDoctorList() {
        viewModelScope.launch {
            doctorRepository.getAllDoctors()
                .collect { result ->
                    when (result) {
                        is AppResult.Success -> {
                            _state.update { it.copy(doctorList = result.data) }
                        }

                        is AppResult.Error -> {
                            // Handle error
                        }
                    }
                }
        }
    }

    fun resetData() {
        _state.value = UpdateBlogUiState()
    }

    private fun updateBlog() {
        val blogState = _state.value
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            blogRepository
                .updateBlog(
                    blog = BlogMaster(
                        id = blogState.blogDetails.id,
                        title = blogState.title,
                        description = blogState.blogDescription,
                        doctorId = blogState.doctor.id,
                        createdAt = blogState.blogDetails.createdAt,
                        updatedAt = getCurrentTimeStamp(),
                        imageUrl = blogState.blogDetails.imageUrl
                    ),
                    imageFile = blogState.imageFile
                )
                .collect { result ->
                    when (result) {
                        is AppResult.Success -> {
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    isSuccessful = true,
                                    updatedBlog = result.data
                                )
                            }
                        }

                        is AppResult.Error -> {
                            _state.update {
                                it.copy(
                                    error = result.error.toString(),
                                    isLoading = false
                                )
                            }
                        }
                    }
                }
        }
    }
}

data class UpdateBlogUiState(
    val isLoading: Boolean = false,
    val isSuccessful: Boolean = false,
    val error: String? = null,
    val title: String = "",
    val blogDescription: String = "",
    val imageFile: File? = null,
    val showDoctorList: Boolean = false,
    val blogDetails: BlogMaster = BlogMaster(),
    val doctorList: List<DoctorMaster> = emptyList(),
    val doctor: DoctorMaster = DoctorMaster(),
    val updatedBlog: BlogMaster? = null
) {
    val isFormValid: Boolean
        get() = title.isNotBlank() && blogDescription.isNotBlank()

    fun getErrorMessage(): String {
        return when {
            title.isBlank() -> "Title is required"
            blogDescription.isBlank() -> "Description is required"
            else -> ""
        }
    }
}

sealed interface UpdateBlogAction {
    data class OnBlogTitleChange(val title: String) : UpdateBlogAction
    data class OnBlogDescriptionChange(val description: String) : UpdateBlogAction
    data class OnDoctorChange(val doctor: DoctorMaster) : UpdateBlogAction
    data class OnImageChange(val file: File?) : UpdateBlogAction
    data class OnShowDoctorListClicked(val clicked: Boolean) : UpdateBlogAction
    data object OnSubmit : UpdateBlogAction
    data object OnCancel : UpdateBlogAction
    data class OnBlogReceive(val blogDetails: BlogMaster) : UpdateBlogAction
}