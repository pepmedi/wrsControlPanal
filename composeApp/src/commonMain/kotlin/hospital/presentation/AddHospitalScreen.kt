package hospital.presentation

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import hospital.domain.HospitalMaster
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import util.FileCompressor
import util.FileUtil.loadAndCompressImage
import util.ToastEvent
import util.getCurrentTimeStamp
import java.io.File

@Composable
fun AddHospitalScreen(viewModel: HospitalViewModel = koinViewModel(), onBackClick: () -> Unit) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var hospitalName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    val toaster = rememberToasterState()
    var toasterEvent by remember { mutableStateOf<ToastEvent?>(null) }

    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var imageFile by remember { mutableStateOf<File?>(null) }

    LaunchedEffect(uiState.hospitalAddedSuccessfully) {
        if (uiState.hospitalAddedSuccessfully) {
            viewModel.onAction(HospitalActions.OnHospitalAddedSuccessfully)
            toaster.show(
                message = "Hospital Added Successfully",
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

    fun validateForm(): Boolean {
        val errors = mutableListOf<String>()

        if (hospitalName.isBlank()) errors.add("Hospital Name is required")
        if (address.isBlank()) errors.add("Address is required")
        if (imageFile == null) errors.add("Logo is required")

        if (errors.isNotEmpty()) {
            scope.launch {
                toasterEvent = ToastEvent(errors.first())
            }
            return false
        }
        return true
    }

    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
            Column(
                modifier = Modifier.fillMaxWidth()
                    .background(Color.White)
                    .padding(10.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextInputField(
                    value = hospitalName,
                    onValueChange = { hospitalName = it },
                    label = "Hospital Name",
                    icon = Icons.Outlined.Home
                )


                TextInputField(
                    value = address,
                    onValueChange = { address = it },
                    label = "Hospital Address",
                    icon = Icons.Outlined.LocationOn
                )


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    //Service image
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
                        text = "Select Hospital Logo Image"
                    )
                }

                if (uiState.isLoading) {
                    CircularProgressIndicator()
                } else {
                    GradientButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            if (validateForm() && imageFile != null) {
                                viewModel.onAction(
                                    HospitalActions.OnAddHospital(
                                        hospitalMaster = HospitalMaster(
                                            id = "",
                                            name = hospitalName,
                                            address = address,
                                            createdAt = getCurrentTimeStamp(),
                                            updatedAt = getCurrentTimeStamp()
                                        ),
                                        imageFile!!
                                    )
                                )
                            }
                        })
                }

                CancelButton(onBackClick)
            }

            Toaster(
                state = toaster,
                richColors = true,
                alignment = Alignment.TopEnd
            )
        }
    }
}