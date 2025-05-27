package doctor.screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
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
import util.BlogElement
import util.DoctorEducationElement
import util.FileCompressor
import util.FileUtil.loadAndCompressImage
import util.ToastEvent
import util.buildDoctorEducationFormatted
import util.buildFormattedBlog
import java.io.File

@Composable
fun UpdateDoctorDetailsScreenRoot(
    doctorId: String,
    viewModel: UpdateDoctorViewModel = koinViewModel(),
    onSuccessful: (DoctorMaster) -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    var toasterEvent by remember { mutableStateOf<ToastEvent?>(null) }
    val toaster = rememberToasterState()

    LaunchedEffect(doctorId) {
        viewModel.getDoctor(doctorId)
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
            viewModel.resetData()
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
            viewModel.onAction(action)
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

    var doctorProfileImageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var doctorProfileImageFile by remember { mutableStateOf<File?>(null) }

    var doctorInfoImageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var doctorInfoImageFile by remember { mutableStateOf<File?>(null) }

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
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    item {
                        TextInputField(
                            value = uiState.doctorDetails.name,
                            onValueChange = { onAction(UpdateDoctorActions.OnDoctorNameChange(it)) },
                            label = "Doctor Name",
                            icon = Icons.Default.Person,
                        )
                    }

                    item {
                        TextInputField(
                            value = uiState.doctorDetails.age,
                            onValueChange = { onAction(UpdateDoctorActions.OnAgeChange(it)) },
                            label = "Age",
                        )
                    }

                    item {
                        TextInputField(
                            value = uiState.doctorDetails.qualification,
                            onValueChange = { onAction(UpdateDoctorActions.OnQualificationChange(it)) },
                            label = "Qualification",
                        )
                    }

                    item {
                        TextInputField(
                            value = uiState.doctorDetails.experience,
                            onValueChange = { onAction(UpdateDoctorActions.OnExperienceChange(it)) },
                            label = "Doctor Experience",
                            icon = Icons.Default.Person,
                        )
                    }

                    item {
                        TextInputField(
                            value = uiState.doctorDetails.speciality,
                            onValueChange = { onAction(UpdateDoctorActions.OnSpecialityChange(it)) },
                            label = "Doctor Speciality",
                        )
                    }

                    item {
                        TextInputField(
                            value = uiState.doctorDetails.profile,
                            onValueChange = { onAction(UpdateDoctorActions.OnProfileTextChange(it)) },
                            label = "Doctor Profile",
                            minLines = 3,
                        )
                    }

                    item {
                        TextInputField(
                            value = uiState.doctorDetails.careerPath,
                            onValueChange = { onAction(UpdateDoctorActions.OnCareerPathChange(it)) },
                            label = "Career Path",
                            minLines = 3,
                        )
                    }

                    item {
                        TextInputField(
                            value = uiState.doctorDetails.focus,
                            onValueChange = { onAction(UpdateDoctorActions.OnFocusChange(it)) },
                            label = "Doctor Focus",
                            minLines = 3,
                        )
                    }

                    items(buildDoctorEducationFormatted(uiState.doctorDetails.focus)) { element ->
                        when (element) {
                            is DoctorEducationElement.Text -> Text(
                                text = element.content,
                                style = LocalTextStyle.current,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            is DoctorEducationElement.Divider -> HorizontalDivider(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp),
                                thickness = 1.dp,
                                color = Color.Gray
                            )
                        }
                    }

                    item {
                        TextInputField(
                            value = uiState.selectedHospitals.joinToString(", ") { it.name },
                            onValueChange = { },
                            label = "Hospital",
                            enabled = false,
                            onClick = { showHospitalList = true }
                        )
                    }


                    item {
                        TextInputField(
                            value = uiState.selectedServices.joinToString(", ") { it.name },
                            onValueChange = { },
                            label = "Services",
                            enabled = false,
                            onClick = { showServiceList = true }
                        )
                    }


                    item {
                        TextInputField(
                            value = uiState.selectedSlots.joinToString(", ") { it.name },
                            onValueChange = { },
                            label = "Slots",
                            enabled = false,
                            onClick = { showSlotsList = true }
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            //first image
                            ImageSelector(
                                text = "Doctor Profile Picture",
                                imageBitmap = doctorProfileImageBitmap,
                                onImageSelected = { file ->
                                    scope.launch {
                                        doctorProfileImageFile =
                                            FileCompressor.loadAndCompressImage(file)
                                        doctorProfileImageBitmap = loadAndCompressImage(file)
                                        onAction(
                                            UpdateDoctorActions.OnProfilePicChange(
                                                doctorProfileImageFile
                                            )
                                        )
                                    }
                                },
                                errorMessage = { message ->
                                    toasterEvent(ToastEvent(message))
                                },
                                imageUrl = uiState.doctorDetails.profilePic
                            )

                            //first image
                            ImageSelector(
                                text = "Doctor Information Image",
                                imageBitmap = doctorInfoImageBitmap,
                                onImageSelected = { file ->
                                    scope.launch {
                                        doctorInfoImageFile =
                                            FileCompressor.loadAndCompressImage(file)
                                        doctorInfoImageBitmap = loadAndCompressImage(file)
                                        onAction(
                                            UpdateDoctorActions.OnDoctorInfoPicChange(
                                                doctorInfoImageFile
                                            )
                                        )
                                    }
                                },
                                errorMessage = { message ->
                                    toasterEvent(ToastEvent(message))
                                },
                                imageUrl = uiState.doctorDetails.doctorInfoPic
                            )
                        }
                    }


                    item {
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