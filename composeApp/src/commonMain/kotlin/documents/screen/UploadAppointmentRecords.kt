package documents.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
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
import com.dokar.sonner.ToastType
import com.dokar.sonner.Toaster
import com.dokar.sonner.rememberToasterState
import component.AppCircularProgressIndicator
import component.GradientButton
import core.CancelButton
import core.FileSelector
import doctor.screen.components.TextInputField
import documents.modal.PatientMedicalRecordsMaster
import documents.viewModal.UploadAppointmentRecordsViewModal
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import util.ToastEvent
import util.getCurrentTimeStamp
import java.io.File

@Composable
fun UploadAppointmentRecords(
    viewModal: UploadAppointmentRecordsViewModal = koinViewModel(),
    appointmentId: String,
    onBackClick: () -> Unit,
    onSuccessfulUpload: (PatientMedicalRecordsMaster) -> Unit
) {

    var toasterEvent by remember { mutableStateOf<ToastEvent?>(null) }
    val toaster = rememberToasterState()

    var recordName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var mimeType by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var imageFile by remember { mutableStateOf<File?>(null) }
    val imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(toasterEvent?.id) {
        toasterEvent?.let {
            toaster.show(
                message = it.message,
                type = it.type,
//                action = TextToastAction(
//                    text = "Done",
//                    onClick = { toaster.dismissAll() }
//                )
            )
        }
    }

    fun validateForm(): Boolean {
        val errors = mutableListOf<String>()

        if (recordName.isBlank()) errors.add("Record Name is required.")
        if (description.isBlank()) errors.add("Description is required.")
        if (imageFile == null) errors.add("Image or Pdf is required.")

        if (errors.isNotEmpty()) {
            toasterEvent = ToastEvent(errors.first())
            return false
        }
        return true
    }

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(10.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextInputField(
                    value = recordName,
                    onValueChange = { recordName = it },
                    label = "Record Name",
                    icon = Icons.Default.Person,
                )

                TextInputField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Description",
                    minLines = 3,
                    icon = Icons.Default.Person,
                )

                FileSelector(
                    imageBitmap = imageBitmap,
                    onFileSelected = { file, mimType ->
                        imageFile = file
                        mimeType = mimType
                    },
                    errorMessage = { message ->
                        toasterEvent = ToastEvent(message)
                    }
                )

                if (isUploading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    AppCircularProgressIndicator()
                } else {
                    GradientButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            if (validateForm()) {
                                isUploading = true
                                toasterEvent =
                                    ToastEvent(message = "Uploading...", type = ToastType.Info)
                                scope.launch {
                                    viewModal.uploadAppointmentRecords(
                                        patientMedicalRecordsMaster = PatientMedicalRecordsMaster(
                                            id = "",
                                            appointmentId = appointmentId,
                                            recordName = recordName,
                                            description = description,
                                            fileUrl = "",
                                            mimeType = "",
                                            storagePath = "",
                                            createdAt = getCurrentTimeStamp(),
                                            isActive = "0"
                                        ),
                                        document = imageFile!!,
                                        mimeType = mimeType
                                    ).await()
                                        .onSuccess {
                                            isUploading = false
                                            toasterEvent = ToastEvent(
                                                message = "Upload Successful",
                                                type = ToastType.Success
                                            )
                                            delay(1500)
                                            onSuccessfulUpload(it)
                                        }
                                        .onFailure { e ->
                                            toasterEvent =
                                                ToastEvent(e.message ?: "Something went wrong")
                                            isUploading = false
                                        }
                                }
                            }
                        }
                    )
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