package blog.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import blog.viewModel.AddBlogAction
import blog.viewModel.AddBlogState
import blog.viewModel.AddBlogViewModel
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
fun AddBlogScreenRoot(viewModel: AddBlogViewModel = koinViewModel(), onBackClick: () -> Unit) {

    val state by viewModel.state.collectAsState()

    val toaster = rememberToasterState()
    var toasterEvent by remember { mutableStateOf<ToastEvent?>(null) }

    LaunchedEffect(state.isSuccessful) {
        if (state.isSuccessful) {
            toaster.show(
                message = "Blog Added Successfully",
                type = ToastType.Success,
                action = TextToastAction(
                    text = "Done",
                    onClick = {
                        toaster.dismissAll()
                    }
                )
            )
            onBackClick()
        }
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

    AddBlogScreen(
        uiState = state,
        onAction = { action ->
            when (action) {
                is AddBlogAction.OnCancel -> onBackClick()
                is AddBlogAction.OnSubmit -> {
                    if (state.isFormValid) {
                        viewModel.onAction(action)
                    } else {
                        toasterEvent = ToastEvent(state.getError())
                    }
                }

                else -> Unit
            }
            viewModel.onAction(action)
        }
    )
    Toaster(
        state = toaster,
        richColors = true,
        alignment = Alignment.TopEnd
    )
}

@Composable
fun AddBlogScreen(
    uiState: AddBlogState,
    onAction: (AddBlogAction) -> Unit
) {
    var imageBitmap1 by remember { mutableStateOf<ImageBitmap?>(null) }
    val scope = rememberCoroutineScope()
    var snackBarMessage by remember { mutableStateOf("") }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.White).padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextInputField(
                value = uiState.title,
                onValueChange = { onAction(AddBlogAction.OnBlogTitleChange(it)) },
                label = "BLog Title",
                icon = Icons.Default.Person,
                maxLines = 2
            )

            TextInputField(
                value = uiState.blogDescription,
                onValueChange = { onAction(AddBlogAction.OnBlogDescriptionChange(it)) },
                label = "BLog Description",
            )

            TextInputField(
                value = uiState.doctor.name.toNameFormat(),
                onValueChange = { onAction(AddBlogAction.OnShowDoctorListClicked(true)) },
                label = "Doctor Name",
                enabled = false,
                icon = Icons.Default.Person
            )

            ImageSelector(
                imageBitmap = imageBitmap1,
                onImageSelected = { file ->
                    scope.launch {
                        onAction(
                            AddBlogAction.OnImageChange(
                                FileCompressor.loadAndCompressImage(
                                    file
                                )
                            )
                        )
                        imageBitmap1 = loadAndCompressImage(file)
                    }
                },
                errorMessage = { message ->
                    snackBarMessage = message
                }
            )

            if (uiState.isUploading) {
                AppCircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                GradientButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Submit",
                    onClick = { onAction(AddBlogAction.OnSubmit) },
                    enable = uiState.isFormValid
                )
            }

            CancelButton({ onAction(AddBlogAction.OnCancel) })
        }

        if (uiState.showDoctorList) {
            DoctorListDialog(doctorList = uiState.doctorList,
                onDismiss = { onAction(AddBlogAction.OnShowDoctorListClicked(false)) },
                onSubmit = { onAction(AddBlogAction.OnDoctorChange(it)) })
        }
    }
}