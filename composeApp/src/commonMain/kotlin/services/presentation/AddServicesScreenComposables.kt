package services.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import core.CancelButton
import core.ImageSelector
import doctor.presentation.components.TextInputField
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import services.domain.ServiceStates
import services.domain.ServicesMaster
import util.FileCompressor
import util.FileUtil.loadAndCompressImage
import util.getCurrentTimeStamp
import java.io.File

@Composable
fun AddServicesScreenUI(viewModal: ServicesViewModal = koinViewModel(), onBackClick: () -> Unit) {
    val servicesState by viewModal.serviceStates.collectAsStateWithLifecycle()

    var serviceName by remember { mutableStateOf("") }

    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var imageFile by remember { mutableStateOf<File?>(null) }

    var iconBitMap by remember { mutableStateOf<ImageBitmap?>(null) }
    var iconFile by remember { mutableStateOf<File?>(null) }

    var snackBarMessage by remember { mutableStateOf("") }
    // Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(servicesState) {
        if (servicesState is ServiceStates.Success) {
            viewModal.resetState() // Reset state
            onBackClick() // Navigate back
        }
    }

    fun validateForm(): Boolean {
        val errors = mutableListOf<String>()

        if (serviceName.isBlank()) errors.add("Service Name is required")
        if(imageBitmap == null) errors.add("Service Image is required")
        if(iconBitMap == null) errors.add("Service Icon is required")

        if (errors.isNotEmpty()) {
            scope.launch {
                snackbarHostState.showSnackbar(errors.joinToString("\n"))
            }
            return false
        }
        return true
    }

    MaterialTheme {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                        .background(Color.White)
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        TextInputField(
                            value = serviceName,
                            onValueChange = { serviceName = it },
                            label = "Service Name",
                            icon = Icons.Outlined.Home
                        )
                    }

                    item {
                        Row {
                            //first image
                            ImageSelector(
                                imageBitmap = imageBitmap,
                                onImageSelected = { file ->
                                    scope.launch {
                                        imageFile = FileCompressor.loadAndCompressImage(file)
                                        imageBitmap = loadAndCompressImage(file)
                                    }
                                },
                                snackBarMessage = { message ->
                                    snackBarMessage = message
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
                                snackBarMessage = { message ->
                                    snackBarMessage = message
                                },
                                text = "Select Service icon"
                            )
                        }
                    }

                    item {
                        if (servicesState == ServiceStates.Loading) {
                            CircularProgressIndicator()
                        } else {
                            Button(modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(5.dp), onClick = {
                                    if (validateForm()) {
                                        scope.launch {
                                            imageFile?.let {
                                                iconFile?.let { it1 ->
                                                    viewModal.addService(
                                                        service = ServicesMaster(
                                                            id = "",
                                                            name = serviceName,
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
                                }) {
                                Text("Submit")
                            }
                        }
                    }
                    item {
                        CancelButton(onBackClick)
                    }
                }
            }
        }
    }
}