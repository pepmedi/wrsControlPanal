package services.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import blog.helper.BlogElement
import blog.helper.buildFormattedBlog
import com.dokar.sonner.TextToastAction
import com.dokar.sonner.ToastType
import com.dokar.sonner.Toaster
import com.dokar.sonner.rememberToasterState
import component.AppCircularProgressIndicator
import component.GradientButton
import core.CancelButton
import core.ImageSelector
import doctor.screen.components.TextInputField
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import services.domain.ServicesMaster
import services.viewModel.UpdateServicesAction
import services.viewModel.UpdateServicesUiState
import services.viewModel.UpdateServicesViewModel
import util.FileCompressor
import util.FileUtil.loadAndCompressImage
import util.ToastEvent
import java.io.File

@Composable
fun UpdateServicesScreenUIRoot(
    service: ServicesMaster,
    viewModel: UpdateServicesViewModel = koinViewModel(),
    onBackClick: () -> Unit,
    onServiceUpdated: (ServicesMaster?) -> Unit
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    val toaster = rememberToasterState()
    var toasterEvent by remember { mutableStateOf<ToastEvent?>(null) }

    LaunchedEffect(service) {
        viewModel.onAction(UpdateServicesAction.OnServiceReceive(service))
    }

    LaunchedEffect(uiState.isSuccessful, uiState.error) {
        if (uiState.isSuccessful) {
            toaster.show(
                message = "Service updated Successfully",
                type = ToastType.Success,
                action = TextToastAction(
                    text = "Done",
                    onClick = {
                        toaster.dismissAll()
                    }
                )
            )
            onServiceUpdated(uiState.updatedService)
            viewModel.resetState()
            onBackClick()
        } else if (uiState.error != null) {
            toasterEvent = ToastEvent(message = "SomeThing went wrong.\n please try again ")
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

    UpdateServicesScreenUI(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                is UpdateServicesAction.OnBackClick -> {
                    viewModel.resetState()
                    onBackClick()
                }

                is UpdateServicesAction.OnSubmit -> {
                    if (!uiState.isFormValid) {
                        toasterEvent = ToastEvent(uiState.getErrorMessage())
                    }
                }

                else -> Unit
            }
            viewModel.onAction(action)
        },
        toasterEvent = {
            toasterEvent = it
        })

    Toaster(
        state = toaster,
        richColors = true,
        alignment = Alignment.TopEnd
    )
}

@Composable
fun UpdateServicesScreenUI(
    uiState: UpdateServicesUiState,
    onAction: (UpdateServicesAction) -> Unit,
    scope: CoroutineScope = rememberCoroutineScope(),
    toasterEvent: (ToastEvent) -> Unit
) {
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var imageFile by remember { mutableStateOf<File?>(null) }

    var iconBitMap by remember { mutableStateOf<ImageBitmap?>(null) }
    var iconFile by remember { mutableStateOf<File?>(null) }

    MaterialTheme {
        Scaffold(
            containerColor = Color.White
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.White)
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                item {
                    TextInputField(
                        value = uiState.serviceName,
                        onValueChange = { onAction(UpdateServicesAction.OnServiceNameChange(it)) },
                        label = "Service Name",
                    )
                }

                item {
                    TextInputField(
                        value = uiState.serviceDescription,
                        onValueChange = {
                            onAction(UpdateServicesAction.OnServiceDescriptionChange(it))
                        },
                        label = "Service Description",
                    )
                }

                // Render blog elements inside LazyColumn items, no nested LazyColumn!
                items(
                    buildFormattedBlog(
                        uiState.serviceDescription,
                        imageList = emptyList()
                    )
                ) { element ->
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ImageSelector(
                            imageBitmap = imageBitmap,
                            onImageSelected = { file ->
                                scope.launch {
                                    imageFile = FileCompressor.loadAndCompressImage(file)
                                    imageBitmap = loadAndCompressImage(file)
                                    onAction(UpdateServicesAction.OnImageChange(imageFile))
                                }
                            },
                            errorMessage = { message -> toasterEvent(ToastEvent(message)) },
                            text = "Select Service Image",
                            imageUrl = uiState.services?.imageUrl
                        )

                        ImageSelector(
                            imageBitmap = iconBitMap,
                            onImageSelected = { file ->
                                scope.launch {
                                    iconFile = FileCompressor.loadAndCompressImage(file)
                                    iconBitMap = loadAndCompressImage(file)
                                    onAction(UpdateServicesAction.OnIconChange(iconFile))
                                }
                            },
                            errorMessage = { message -> toasterEvent(ToastEvent(message)) },
                            text = "Select Service icon",
                            imageUrl = uiState.services?.iconUrl
                        )
                    }
                }

                item {
                    if (uiState.isLoading) {
                        AppCircularProgressIndicator()
                    } else {
                        GradientButton(
                            modifier = Modifier.fillMaxWidth(),
                            enable = uiState.isFormValid,
                            onClick = { onAction(UpdateServicesAction.OnSubmit) }
                        )
                    }
                }

                item {
                    CancelButton(onBackClick = { onAction(UpdateServicesAction.OnBackClick) })
                }
            }
        }
    }
}
