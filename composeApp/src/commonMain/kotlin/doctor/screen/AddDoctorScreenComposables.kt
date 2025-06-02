package doctor.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dokar.sonner.TextToastAction
import com.dokar.sonner.ToastType
import com.dokar.sonner.Toaster
import com.dokar.sonner.rememberToasterState
import component.GradientButton
import core.CancelButton
import core.ImageSelector
import doctor.domain.DoctorMaster
import doctor.domain.DoctorSubmissionState
import doctor.screen.components.TextInputField
import doctor.viewModal.AddDoctorViewModel
import hospital.domain.HospitalMaster
import hospital.presentation.components.HospitalListDialog
import hospital.presentation.components.ServicesListDialog
import hospital.presentation.components.SlotsListDialog
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import services.domain.ServicesMaster
import slots.domain.SlotsMaster
import util.DoctorEducationElement
import util.FileCompressor
import util.FileUtil.loadAndCompressImage
import util.ToastEvent
import util.buildDoctorEducationFormatted
import util.getCurrentTimeStamp
import java.io.File

@Composable
fun AddDoctorScreen(
    viewModal: AddDoctorViewModel = koinViewModel(),
    onBackClick: () -> Unit,
    onDoctorAdded: (DoctorMaster) -> Unit
) {

    val doctorSubmissionState by viewModal.doctorSubmissionState.collectAsStateWithLifecycle()
    val hospitalListState = viewModal.hospitalList.collectAsStateWithLifecycle()
    val servicesListState = viewModal.servicesList.collectAsStateWithLifecycle()
    val slotsMaster = viewModal.slotsList.collectAsStateWithLifecycle()
    var doctorName by remember { mutableStateOf("") }
    var doctorExperience by remember { mutableStateOf("") }
    var doctorCity by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }

    var qualification by remember { mutableStateOf("") }

    var speciality by remember { mutableStateOf("") }

    var doctorProfileImageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var doctorProfileImageFile by remember { mutableStateOf<File?>(null) }

    var doctorInfoImageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var doctorInfoImageFile by remember { mutableStateOf<File?>(null) }

    var doctorHomePageImageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var doctorHomePageImageFile by remember { mutableStateOf<File?>(null) }

    val scope = rememberCoroutineScope()

    var showHospitalList by remember { mutableStateOf(false) }
    var selectedHospitals by remember { mutableStateOf(emptyList<HospitalMaster>()) }

    var showServiceList by remember { mutableStateOf(false) }
    var selectedServices by remember { mutableStateOf(emptyList<ServicesMaster>()) }

    var showSlotsList by remember { mutableStateOf(false) }
    var selectedSlots by remember { mutableStateOf(emptyList<SlotsMaster>()) }

    var toasterEvent by remember { mutableStateOf<ToastEvent?>(null) }
    val toaster = rememberToasterState()

    var doctorProfileText by remember { mutableStateOf("") }
    var doctorCareerPath by remember { mutableStateOf("") }
    var doctorFocus by remember { mutableStateOf("") }


    LaunchedEffect(doctorSubmissionState) {
        val state = doctorSubmissionState
        if (state is DoctorSubmissionState.Success) {
            viewModal.resetSubmissionState()
            toaster.show(
                message = "Doctor Added Successfully",
                type = ToastType.Success,
                action = TextToastAction(
                    text = "Done",
                    onClick = {
                        toaster.dismissAll()
                    }
                )
            )
            onDoctorAdded(state.doctor)
            viewModal.reset()
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

        if (doctorName.isBlank()) errors.add("Doctor Name is required.")
        if (doctorExperience.isBlank()) errors.add("Experience is required.")
        if (doctorCity.isBlank()) errors.add("City is required.")
        if (age.isBlank()) errors.add("Age is required.")
        if (doctorProfileText.isBlank()) errors.add("Profile is required.")
        if (doctorCareerPath.isBlank()) errors.add("Career Path is required.")
        if (doctorFocus.isBlank()) errors.add("Focus is required.")
        if (selectedHospitals.isEmpty()) errors.add("Hospital is required.")
        if (selectedServices.isEmpty()) errors.add("Services is required.")
        if (qualification.isBlank()) errors.add("Qualification Fee is required.")
        if (speciality.isBlank()) errors.add("Speciality is required.")
        if (selectedSlots.isEmpty()) errors.add("Slots Are required")
        if (doctorProfileImageFile == null) errors.add("Profile Image is required")
        if (doctorInfoImageFile == null) errors.add("Info Image is required")
        if (doctorHomePageImageFile == null) errors.add("Home Page Image is required")

        if (errors.isNotEmpty()) {
            toasterEvent = ToastEvent(errors.first())
            return false
        }
        return true
    }

    MaterialTheme {
        Scaffold { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(paddingValues)
            ) {

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
                            value = doctorName,
                            onValueChange = { doctorName = it },
                            label = "Doctor Name",
                            icon = Icons.Default.Person,
                        )
                    }


                    item {
                        TextInputField(
                            value = age,
                            onValueChange = { age = it },
                            label = "Age",
                        )
                    }

                    item {
                        TextInputField(
                            value = qualification,
                            onValueChange = { qualification = it },
                            label = "Qualification",
                        )
                    }

                    item {
                        TextInputField(
                            value = doctorExperience,
                            onValueChange = { doctorExperience = it },
                            label = "Doctor Experience",
                        )
                    }

                    item {
                        TextInputField(
                            value = doctorCity,
                            onValueChange = { doctorCity = it },
                            label = "Doctor City",
                        )
                    }

                    item {
                        TextInputField(
                            value = speciality,
                            onValueChange = { speciality = it },
                            label = "Doctor Speciality",
                        )
                    }

                    item {
                        TextInputField(
                            value = doctorProfileText,
                            onValueChange = { doctorProfileText = it },
                            label = "Doctor Profile",
                            minLines = 3,
                        )
                    }

                    item {
                        TextInputField(
                            value = doctorCareerPath,
                            onValueChange = { doctorCareerPath = it },
                            label = "Career Path",
                            minLines = 3,
                        )
                    }

                    item {
                        TextInputField(
                            value = doctorFocus,
                            onValueChange = { doctorFocus = it },
                            label = "Doctor Education Details",
                            minLines = 3,
                        )
                    }

                    // Render blog elements as items here instead of RenderBlog (no nested LazyColumn)
                    val elements = buildDoctorEducationFormatted(doctorFocus)
                    items(elements) { element ->
                        when (element) {
                            is DoctorEducationElement.Text -> Text(
                                text = element.content,
                                style = LocalTextStyle.current.copy(
                                    lineHeight = 20.sp,
                                    textAlign = TextAlign.Start
                                ),
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
                            value = selectedHospitals.joinToString(", ") { it.name },
                            onValueChange = { },
                            label = "Hospital",
                            icon = Icons.Default.Home,
                            enabled = false,
                            onClick = { showHospitalList = true }
                        )
                    }

                    item {
                        TextInputField(
                            value = selectedServices.joinToString(", ") { it.name },
                            onValueChange = { },
                            label = "Services",
                            enabled = false,
                            onClick = { showServiceList = true }
                        )
                    }

                    item {
                        TextInputField(
                            value = selectedSlots.joinToString(", ") { it.name },
                            onValueChange = { },
                            label = "Slots",
                            enabled = false,
                            onClick = { showSlotsList = true }
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {

                            ImageSelector(
                                text = "Doctor Profile Picture",
                                imageBitmap = doctorProfileImageBitmap,
                                onImageSelected = { file ->
                                    scope.launch {
                                        doctorProfileImageFile =
                                            FileCompressor.loadAndCompressImage(file)
                                        doctorProfileImageBitmap = loadAndCompressImage(file)
                                    }
                                },
                                errorMessage = { message ->
                                    toasterEvent = ToastEvent(message)
                                })

                            ImageSelector(
                                text = "Doctor Information Image",
                                imageBitmap = doctorInfoImageBitmap,
                                onImageSelected = { file ->
                                    scope.launch {
                                        doctorInfoImageFile = FileCompressor.loadAndCompressImage(
                                            file,
                                            compressionThreshold = 250
                                        )
                                        doctorInfoImageBitmap = loadAndCompressImage(file)
                                    }
                                },
                                errorMessage = { message ->
                                    toasterEvent = ToastEvent(message)
                                })

                            ImageSelector(
                                text = "Doctor Home Page Image",
                                imageBitmap = doctorHomePageImageBitmap,
                                onImageSelected = { file ->
                                    scope.launch {
                                        doctorHomePageImageFile =
                                            FileCompressor.loadAndCompressImage(
                                                file,
                                                compressionThreshold = 250
                                            )
                                        doctorHomePageImageBitmap = loadAndCompressImage(file)
                                    }
                                },
                                errorMessage = { message ->
                                    toasterEvent = ToastEvent(message)
                                })
                        }
                    }

                    item {
                        if (doctorSubmissionState == DoctorSubmissionState.Loading) {
                            CircularProgressIndicator()
                        } else {
                            GradientButton(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    if (validateForm()) {
                                        scope.launch {
                                            doctorInfoImageFile?.let { doctorImage ->
                                                viewModal.addDoctor(
                                                    doctorsMaster = DoctorMaster(
                                                        id = "",
                                                        name = doctorName,
                                                        experience = doctorExperience,
                                                        profilePic = "",
                                                        age = age,
                                                        services = selectedServices.map { it.id },
                                                        hospital = selectedHospitals.map { it.id },
                                                        qualification = qualification,
                                                        reviews = "",
                                                        city = doctorCity,
                                                        careerPath = doctorCareerPath,
                                                        focus = doctorFocus,
                                                        profile = doctorProfileText,
                                                        slots = selectedSlots.map { it.id },
                                                        updatedAt = getCurrentTimeStamp(),
                                                        createdAt = getCurrentTimeStamp()
                                                    ),
                                                    doctorProfileImage = doctorImage,
                                                    doctorInfoImage = doctorInfoImageFile!!,
                                                    doctorHomePageImage = doctorHomePageImageFile!!
                                                )
                                            }
                                        }
                                    }
                                })
                        }

                        CancelButton(onBackClick)
                    }

                }
                Toaster(
                    state = toaster,
                    richColors = true,
                    alignment = Alignment.TopEnd
                )

                if (showHospitalList) {
                    HospitalListDialog(
                        hospitalResult = hospitalListState.value,
                        selectedHospitalList = selectedHospitals,
                        onDismiss = { showHospitalList = false },
                        onSubmit = { selectedHospitals = it }
                    )
                }

                if (showServiceList) {
                    ServicesListDialog(
                        serviceResult = servicesListState.value,
                        selectedServicesList = selectedServices,
                        onDismiss = { showServiceList = false },
                        onSubmit = {
                            selectedServices = it
                        }
                    )
                }
                if (showSlotsList) {
                    SlotsListDialog(
                        slotsResult = slotsMaster.value,
                        selectedSlotsList = selectedSlots,
                        onDismiss = { showSlotsList = false },
                        onSubmit = {
                            selectedSlots = it
                        }
                    )
                }
            }
        }
    }
}