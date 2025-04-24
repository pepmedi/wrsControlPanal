package doctor.screen.components

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
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.MaterialTheme
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
import com.dokar.sonner.TextToastAction
import com.dokar.sonner.ToastType
import com.dokar.sonner.Toaster
import com.dokar.sonner.rememberToasterState
import component.AppCircularProgressIndicator
import component.GradientButton
import core.CancelButton
import core.ImageSelector
import doctor.domain.DoctorMaster
import doctor.viewModal.UpdateDoctorActions
import doctor.viewModal.UpdateDoctorUiState
import doctor.viewModal.UpdateDoctorViewModel
import hospital.presentation.components.HospitalListDialog
import hospital.presentation.components.ServicesListDialog
import hospital.presentation.components.SlotsListDialog
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import util.FileCompressor
import util.FileUtil.loadAndCompressImage
import util.ToastEvent
import java.io.File

@Composable
fun UpdateDoctorDetailsScreenRoot(
    doctorId: String,
    viewModal: UpdateDoctorViewModel = koinViewModel(),
    onSuccessful: (DoctorMaster) -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModal.state.collectAsStateWithLifecycle()

    var toasterEvent by remember { mutableStateOf<ToastEvent?>(null) }
    val toaster = rememberToasterState()

    LaunchedEffect(doctorId) {
        viewModal.getDoctor(doctorId)
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
            onSuccessful(uiState.doctorDetails)
            viewModal.resetData()
            onBackClick()
        } else if (uiState.error != null) {
            toasterEvent = ToastEvent(message = "SomeThing went wrong.\n please try again ")
        }
    }

    UpdateDoctorDetailsScreen(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                is UpdateDoctorActions.OnUpdateClick -> {
                    if (!uiState.isFormValid) {
                        toasterEvent = ToastEvent(uiState.getErrorMessage())
                    }
                }

                else -> Unit
            }
            viewModal.onAction(action)
        },
        toasterEvent = {
            toasterEvent = it
        },
        onBackClick
    )
    Toaster(
        state = toaster,
        richColors = true,
        alignment = Alignment.TopEnd
    )
}

@Composable
fun UpdateDoctorDetailsScreen(
    uiState: UpdateDoctorUiState,
    onAction: (UpdateDoctorActions) -> Unit,
    toasterEvent: (ToastEvent) -> Unit,
    onBackClick: () -> Unit
) {
    var showHospitalList by remember { mutableStateOf(false) }
    var showServiceList by remember { mutableStateOf(false) }
    var showSlotsList by remember { mutableStateOf(false) }

    var imageBitmap1 by remember { mutableStateOf<ImageBitmap?>(null) }
    var imageFile1 by remember { mutableStateOf<File?>(null) }

    val scope = rememberCoroutineScope()

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            if (uiState.isLoading) {
                AppCircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

            } else if (uiState.error != null) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Text(text = uiState.error)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(10.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    TextInputField(
                        value = uiState.doctorDetails.name,
                        onValueChange = { onAction(UpdateDoctorActions.OnDoctorNameChange(it)) },
                        label = "Doctor Name",
                        icon = Icons.Default.Person,
                    )

                    TextInputField(
                        value = uiState.doctorDetails.age,
                        onValueChange = { onAction(UpdateDoctorActions.OnAgeChange(it)) },
                        label = "Age",
                        icon = Icons.Default.Person,
                    )

                    TextInputField(
                        value = uiState.doctorDetails.consltFee,
                        onValueChange = { onAction(UpdateDoctorActions.OnConsultationFeeChange(it)) },
                        label = "Consultation Fee",
                        icon = Icons.Default.Person,
                    )

                    TextInputField(
                        value = uiState.doctorDetails.experience,
                        onValueChange = { onAction(UpdateDoctorActions.OnExperienceChange(it)) },
                        label = "Doctor Experience",
                        icon = Icons.Default.Person,
                    )

                    TextInputField(
                        value = uiState.doctorDetails.speciality,
                        onValueChange = { onAction(UpdateDoctorActions.OnSpecialityChange(it)) },
                        label = "Doctor Speciality",
                        icon = Icons.Default.Person,
                    )

                    TextInputField(
                        value = uiState.doctorDetails.profile,
                        onValueChange = { onAction(UpdateDoctorActions.OnProfileTextChange(it)) },
                        label = "Doctor Profile",
                        icon = Icons.Default.Person,
                        minLines = 3,
                    )

                    TextInputField(
                        value = uiState.doctorDetails.careerPath,
                        onValueChange = { onAction(UpdateDoctorActions.OnCareerPathChange(it)) },
                        label = "Career Path",
                        icon = Icons.Default.Person,
                        minLines = 3,
                    )

                    TextInputField(
                        value = uiState.doctorDetails.focus,
                        onValueChange = { onAction(UpdateDoctorActions.OnFocusChange(it)) },
                        label = "Doctor Focus",
                        icon = Icons.Default.Person,
                        minLines = 3,
                    )

                    TextInputField(
                        value = uiState.selectedHospitals.joinToString(", ") { it.name },
                        onValueChange = { },
                        label = "Hospital",
                        icon = Icons.Default.Person,
                        enabled = false,
                        onClick = { showHospitalList = true }
                    )

                    TextInputField(
                        value = uiState.selectedServices.joinToString(", ") { it.name },
                        onValueChange = { },
                        label = "Services",
                        icon = Icons.Default.Person,
                        enabled = false,
                        onClick = { showServiceList = true }
                    )

                    TextInputField(
                        value = uiState.selectedSlots.joinToString(", ") { it.name },
                        onValueChange = { },
                        label = "Slots",
                        icon = Icons.Outlined.CheckCircle,
                        enabled = false,
                        onClick = { showSlotsList = true }
                    )

                    //first image
                    ImageSelector(
                        imageBitmap = imageBitmap1,
                        onImageSelected = { file ->
                            scope.launch {
                                imageFile1 = FileCompressor.loadAndCompressImage(file)
                                imageBitmap1 = loadAndCompressImage(file)
                                onAction(UpdateDoctorActions.OnProfilePicChange(imageFile1))
                            }
                        },
                        errorMessage = { message ->
                            toasterEvent(ToastEvent(message))
                        },
                        imageUrl = uiState.doctorDetails.profilePic
                    )

                    if (uiState.isUpdating) {
                        AppCircularProgressIndicator()
                    } else {
                        GradientButton(modifier = Modifier.fillMaxWidth(), onClick = {
                            onAction(UpdateDoctorActions.OnUpdateClick)
                        })
                    }

                    CancelButton(onBackClick)
                }
            }

            if (showHospitalList) {
                HospitalListDialog(
                    hospitalList = uiState.hospitalList,
                    onDismiss = { showHospitalList = false },
                    selectedHospitalList = uiState.selectedHospitals,
                    onSubmit = { onAction(UpdateDoctorActions.OnHospitalChange(it)) }
                )
            }

            if (showServiceList) {
                ServicesListDialog(
                    servicesList = uiState.servicesList,
                    selectedServicesList = uiState.selectedServices,
                    onDismiss = { showServiceList = false },
                    onSubmit = {
                        onAction(UpdateDoctorActions.OnServicesChange(it))
                    }
                )
            }
            if (showSlotsList) {
                SlotsListDialog(
                    slotsList = uiState.slotsList,
                    selectedSlotsList = uiState.selectedSlots,
                    onDismiss = { showSlotsList = false },
                    onSubmit = {
                        onAction(UpdateDoctorActions.OnSlotsChange(it))
                    }
                )
            }
        }
    }
}