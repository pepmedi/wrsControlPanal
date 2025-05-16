package blog.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import blog.domain.BlogMaster
import blog.domain.BlogRepository
import core.domain.AppResult
import core.domain.onError
import core.domain.onSuccess
import doctor.domain.DoctorMaster
import doctor.domain.DoctorRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import util.getCurrentTimeStamp
import java.io.File

class AddBlogViewModel(
    private var blogRepository: BlogRepository,
    private val doctorRepository: DoctorRepository
) : ViewModel() {
    private val _state = MutableStateFlow(AddBlogState())
    val state = _state.asStateFlow()
        .onStart {
            getDoctorList()
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            _state.value
        )

    fun onAction(action: AddBlogAction) {
        when (action) {
            is AddBlogAction.OnBlogTitleChange -> {
                _state.update { it.copy(title = action.title) }
            }

            is AddBlogAction.OnBlogDescriptionChange -> {
                _state.update { it.copy(blogDescription = action.description) }
            }

            is AddBlogAction.OnDoctorChange -> {
                _state.update { it.copy(doctor = action.doctor) }
            }

            is AddBlogAction.OnImageChange -> {
                _state.update { it.copy(imageFile = action.file) }
            }

            is AddBlogAction.OnSubmit -> {
                if (_state.value.isFormValid) {
                    addBlogToDatabase()
                }
            }

            is AddBlogAction.OnShowDoctorListClicked -> {
                _state.update { it.copy(showDoctorList = action.clicked) }
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

    fun reset() {
        _state.value = AddBlogState()
    }

    private fun addBlogToDatabase() {
        val state = _state.value
        _state.update { it.copy(isUploading = true) }
        viewModelScope.launch {
            state.imageFile?.let { blogImage ->
                blogRepository.addBlogToDatabase(
                    imageFile = blogImage,
                    blog = BlogMaster(
                        title = state.title,
                        description = state.blogDescription,
                        createdAt = getCurrentTimeStamp(),
                        updatedAt = getCurrentTimeStamp(),
                        doctorId = state.doctor.id
                    )
                )
                    .onSuccess { addedBlog ->
                        _state.update {
                            it.copy(
                                isSuccessful = true,
                                isUploading = false,
                                addedBlog = addedBlog
                            )
                        }
                    }
                    .onError {
                        _state.update { it.copy(isSuccessful = false, isUploading = false) }
                    }
            }
        }
    }
}

data class AddBlogState(
    val title: String = "",
    val blogDescription: String = "",
    val imageFile: File? = null,
    val doctor: DoctorMaster = DoctorMaster(),
    val showDoctorList: Boolean = false,
    val isUploading: Boolean = false,
    val isSuccessful: Boolean = false,
    val addedBlog: BlogMaster? = null,
    val doctorList: List<DoctorMaster> = emptyList(),
) {
    val isFormValid: Boolean
        get() = title.isNotBlank() && blogDescription.isNotBlank() && imageFile != null
//                && doctor.id.isNotBlank()

    fun getError(): String {
        return when {
            title.isBlank() -> "Title is required"
            blogDescription.isBlank() -> "Description is required"
            imageFile == null -> "Image is required"
//            doctor.id.isBlank() -> "Doctor is required"
            else -> ""
        }
    }
}

sealed interface AddBlogAction {
    data class OnBlogTitleChange(val title: String) : AddBlogAction
    data class OnBlogDescriptionChange(val description: String) : AddBlogAction
    data class OnDoctorChange(val doctor: DoctorMaster) : AddBlogAction
    data class OnImageChange(val file: File?) : AddBlogAction
    data class OnShowDoctorListClicked(val clicked: Boolean) : AddBlogAction
    data object OnSubmit : AddBlogAction
    data object OnCancel : AddBlogAction
}
