package services.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dokar.sonner.TextToastAction
import com.dokar.sonner.ToastType
import com.dokar.sonner.Toaster
import com.dokar.sonner.rememberToasterState
import component.GradientButton
import core.CancelButton
import core.ImageSelector
import doctor.screen.components.TextInputField
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import services.domain.ServiceStates
import services.domain.ServicesMaster
import services.viewModel.ServicesViewModel
import util.FileCompressor
import util.FileUtil.loadAndCompressImage
import util.ToastEvent
import util.getCurrentTimeStamp
import java.io.File

@Composable
fun AddServicesScreenUI(
    viewModal: ServicesViewModel = koinViewModel(),
    onBackClick: () -> Unit,
    onServiceAdded: (ServicesMaster?) -> Unit
) {
    val servicesState by viewModal.serviceStates.collectAsStateWithLifecycle()

    var serviceName by remember { mutableStateOf("") }
    var serviceDescription by remember { mutableStateOf("") }

    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var imageFile by remember { mutableStateOf<File?>(null) }

    var iconBitMap by remember { mutableStateOf<ImageBitmap?>(null) }
    var iconFile by remember { mutableStateOf<File?>(null) }

    val scope = rememberCoroutineScope()

    val toaster = rememberToasterState()
    var toasterEvent by remember { mutableStateOf<ToastEvent?>(null) }

    LaunchedEffect(servicesState) {
        val state = servicesState
        if (state is ServiceStates.Success) {
            viewModal.resetState()
            toaster.show(
                message = "Service Added Successfully",
                type = ToastType.Success,
                action = TextToastAction(
                    text = "Done",
                    onClick = {
                        toaster.dismissAll()
                    }
                )
            )
            onServiceAdded(state.addedService)
            viewModal.resetState()
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

    fun validateForm(): Boolean {
        val errors = mutableListOf<String>()

        if (serviceName.isBlank()) errors.add("Service Name is required")
        if (serviceDescription.isBlank()) errors.add("ServiceDescription Name is required")
        if (imageBitmap == null) errors.add("Service Image is required")
        if (iconBitMap == null) errors.add("Service Icon is required")

        if (errors.isNotEmpty()) {
            toasterEvent = ToastEvent(errors.first())
            return false
        }
        return true
    }

    MaterialTheme {
        Scaffold(
            containerColor = Color.White
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .background(Color.White)
                        .padding(10.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    TextInputField(
                        value = serviceName,
                        onValueChange = { serviceName = it },
                        label = "Service Name",
                    )

                    TextInputField(
                        value = serviceDescription,
                        onValueChange = { serviceDescription = it },
                        label = "Service Description",
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        //first image
                        ImageSelector(
                            imageBitmap = imageBitmap,
                            onImageSelected = { file ->
                                scope.launch {
                                    imageFile = FileCompressor.loadAndCompressImage(file)
                                    imageBitmap = loadAndCompressImage(file)
                                }
                            },
                            errorMessage = { message ->
                                toasterEvent = ToastEvent(message)
                            },
                            text = "Select Service Image"
                        )

                        //first image
                        ImageSelector(
                            imageBitmap = iconBitMap,
                            onImageSelected = { file ->
                                scope.launch {
                                    iconFile = FileCompressor.loadAndCompressImage(file)
                                    iconBitMap = loadAndCompressImage(file)
                                }
                            },
                            errorMessage = { message ->
                                toasterEvent = ToastEvent(message)
                            },
                            text = "Select Service icon"
                        )
                    }

                    if (servicesState == ServiceStates.Loading) {
                        CircularProgressIndicator()
                    } else {
                        GradientButton(
                            modifier = Modifier.fillMaxWidth(),
                            enable = validateForm(),
                            onClick = {
                                if (validateForm()) {
                                    scope.launch {
                                        imageFile?.let {
                                            iconFile?.let { it1 ->
                                                viewModal.addService(
                                                    service = ServicesMaster(
                                                        id = "",
                                                        name = serviceName,
                                                        description = serviceDescription,
                                                        createdAt = getCurrentTimeStamp(),
                                                        updatedAt = getCurrentTimeStamp()
                                                    ),
                                                    imageFile = it,
                                                    iconFile = it1
                                                )
                                            }
                                        }
                                    }
                                }
                            })
                    }

                    CancelButton(onBackClick = {
                        viewModal.resetState()
                        onBackClick()
                    })

                }

                Toaster(
                    state = toaster,
                    richColors = true,
                    alignment = Alignment.TopEnd
                )
            }
        }
    }
}