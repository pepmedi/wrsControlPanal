package blog.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import blog.domain.BlogMaster
import blog.domain.BlogRepository
import core.domain.AppResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AllBLogListViewModel(private val blogRepository: BlogRepository) : ViewModel() {

    private val _state = MutableStateFlow(BLogListUiState())
    val state = _state.asStateFlow()
        .onStart {
            getAllBlog()
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            _state.value
        )

    private fun getAllBlog() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            blogRepository
                .getAllBlogs()
                .collect { result ->
                    when (result) {
                        is AppResult.Success -> {
                            _state.update { it.copy(blogList = result.data, isLoading = false) }
                        }

                        is AppResult.Error -> {
                            _state.update { it.copy(error = result.error.name, isLoading = false) }
                        }
                    }
                }

        }
    }

    fun onAction(action: BlogListActions) {
        when (action) {
            is BlogListActions.OnBlogAdded -> {
                _state.update { it.copy(blogList = it.blogList + action.blog) }
            }

            is BlogListActions.OnBlogUpdated -> {
                _state.update {
                    it.copy(blogList = it.blogList.map { blog ->
                        if (blog.id == action.blog.id) action.blog else blog
                    })
                }
            }

            is BlogListActions.ChangeBlogStatus -> {
                viewModelScope.launch {
                    blogRepository.updateBlog(
                        action.blog.copy(blogActive = if (action.blog.blogActive == "1") "0" else "1"),
                        imageFile = null
                    ).collect { result ->
                        when (result) {
                            is AppResult.Success -> {
                                _state.update {
                                    it.copy(blogList = it.blogList.map { blog ->
                                        if (blog.id == result.data.id) result.data else blog
                                    })
                                }
                            }

                            is AppResult.Error -> {

                            }
                        }

                    }
                }
            }
        }
    }
}

data class BLogListUiState(
    val blogList: List<BlogMaster> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface BlogListActions {
    data class OnBlogUpdated(val blog: BlogMaster) : BlogListActions
    data class OnBlogAdded(val blog: BlogMaster) : BlogListActions
    data class ChangeBlogStatus(val blog: BlogMaster) : BlogListActions
}

