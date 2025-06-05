package blog.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import blog.domain.BlogMaster
import blog.helper.BlogElement
import blog.helper.buildFormattedBlog
import blog.viewModel.UpdateBlogAction
import blog.viewModel.UpdateBlogUiState
import blog.viewModel.UpdateBlogViewModel
import com.dokar.sonner.TextToastAction
import com.dokar.sonner.ToastType
import com.dokar.sonner.Toaster
import com.dokar.sonner.rememberToasterState
import component.AppCircularProgressIndicator
import component.GradientButton
import controlPanalUser.presentation.component.DoctorListDialog
import core.CancelButton
import core.ImageSelector
import doctor.screen.components.TextInputField
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import util.FileCompressor
import util.FileUtil.loadAndCompressImage
import util.ToastEvent
import util.Util.toNameFormat

@Composable
fun UpdateBlogScreenRoot(
    blog: BlogMaster,
    viewModel: UpdateBlogViewModel = koinViewModel(),
    onBack: () -> Unit,
    onBlogUpdated: (BlogMaster?) -> Unit
) {

    val uiState by viewModel.state.collectAsStateWithLifecycle()
    var toasterEvent by remember { mutableStateOf<ToastEvent?>(null) }
    val toaster = rememberToasterState()

    UpdateBlogScreen(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                is UpdateBlogAction.OnCancel -> {
                    viewModel.resetData()
                    onBack()
                }

                is UpdateBlogAction.OnSubmit -> {
                    if (!uiState.isFormValid) {
                        toasterEvent = ToastEvent(uiState.getErrorMessage())
                    }
                }

                else -> Unit
            }
            viewModel.onAction(action)
        })


    LaunchedEffect(blog) {
        viewModel.onAction(UpdateBlogAction.OnBlogReceive(blog))
    }

    LaunchedEffect(toasterEvent?.id) {
        toasterEvent?.let {
            toaster.show(
                message = it.message,
                type = ToastType.Error,
                action = TextToastAction(
                    text = "Done",
                    onClick = { toaster.dismissAll() }
                )
            )
        }
    }

    LaunchedEffect(uiState.isSuccessful, uiState.error) {
        if (uiState.isSuccessful) {
            toaster.show(
                message = "Doctor Data updated successfully",
                type = ToastType.Success
            )
            onBlogUpdated(uiState.updatedBlog)
            viewModel.resetData()
            onBack()
        } else if (uiState.error != null) {
            toasterEvent = ToastEvent(message = "SomeThing went wrong.\n please try again ")
        }
    }

    Toaster(
        state = toaster,
        richColors = true,
        alignment = Alignment.TopEnd
    )
}

@Composable
fun UpdateBlogScreen(
    uiState: UpdateBlogUiState,
    onAction: (UpdateBlogAction) -> Unit
) {
    var imageBitmap1 by remember { mutableStateOf<ImageBitmap?>(null) }
    val scope = rememberCoroutineScope()
    var snackBarMessage by remember { mutableStateOf("") }
    var showDoctorList by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(10.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                TextInputField(
                    value = uiState.title,
                    onValueChange = { onAction(UpdateBlogAction.OnBlogTitleChange(it)) },
                    label = "Blog Title",
                    icon = Icons.Default.Person,
                    maxLines = 2
                )
            }

            item {
                TextInputField(
                    value = uiState.blogDescription,
                    onValueChange = { onAction(UpdateBlogAction.OnBlogDescriptionChange(it)) },
                    label = "Blog Description"
                )
            }

            // Render blog elements as items here instead of RenderBlog (no nested LazyColumn)
            val blogElements = buildFormattedBlog(uiState.blogDescription)
            items(blogElements) { element ->
                when (element) {
                    is BlogElement.Text -> Text(
                        text = element.content,
                        style = LocalTextStyle.current.copy(lineHeight = 20.sp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    BlogElement.Divider -> HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        thickness = 1.dp,
                        color = Color.Gray
                    )

                    is BlogElement.Image -> {}
                }
            }

            item {
                TextInputField(
                    value = uiState.doctor.name.toNameFormat(),
                    onValueChange = {
                        onAction(UpdateBlogAction.OnShowDoctorListClicked(true))
                        showDoctorList = true
                    },
                    label = "Doctor Name",
                    enabled = false,
                    icon = Icons.Default.Person
                )
            }

            item {
                ImageSelector(
                    imageBitmap = imageBitmap1,
                    onImageSelected = { file ->
                        scope.launch {
                            onAction(
                                UpdateBlogAction.OnImageChange(
                                    FileCompressor.loadAndCompressImage(file)
                                )
                            )
                            imageBitmap1 = loadAndCompressImage(file)
                        }
                    },
                    errorMessage = { message -> snackBarMessage = message },
                    imageUrl = uiState.blogDetails.imageUrl
                )
            }

            item {
                if (uiState.isLoading) {
                    AppCircularProgressIndicator()
                } else {
                    GradientButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = "Update",
                        onClick = { onAction(UpdateBlogAction.OnSubmit) },
                        enable = uiState.isFormValid
                    )
                }
            }

            item {
                CancelButton(onBackClick = { onAction(UpdateBlogAction.OnCancel) })
            }
        }
    }

    if (showDoctorList) {
        DoctorListDialog(
            doctorList = uiState.doctorList,
            onDismiss = {
                onAction(UpdateBlogAction.OnShowDoctorListClicked(false))
                showDoctorList = false
            },
            onSubmit = { onAction(UpdateBlogAction.OnDoctorChange(it)) }
        )
    }
}
