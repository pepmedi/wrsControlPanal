package doctor.screen

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
import util.FileCompressor
import util.FileUtil.loadAndCompressImage
import util.ToastEvent
import util.getCurrentTimeStamp
import java.io.File

@Composable
fun AddDoctorScreen(viewModal: AddDoctorViewModel = koinViewModel(), onBackClick: () -> Unit) {

    val doctorSubmissionState by viewModal.doctorSubmissionState.collectAsStateWithLifecycle()
    val hospitalListState = viewModal.hospitalList.collectAsStateWithLifecycle()
    val servicesListState = viewModal.servicesList.collectAsStateWithLifecycle()
    val slotsMaster = viewModal.slotsList.collectAsStateWithLifecycle()

    var doctorName by remember { mutableStateOf("") }
    var doctorExperience by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }

    var consultantFee by remember { mutableStateOf("") }

    var speciality by remember { mutableStateOf("") }
    var imageBitmap1 by remember { mutableStateOf<ImageBitmap?>(null) }
    var imageFile1 by remember { mutableStateOf<File?>(null) }

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
        if (doctorSubmissionState is DoctorSubmissionState.Success) {
            viewModal.resetSubmissionState() // Reset state
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
            onBackClick() // Navigate back
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
        if (age.isBlank()) errors.add("Age is required.")
        if (doctorProfileText.isBlank()) errors.add("Profile is required.")
        if (doctorCareerPath.isBlank()) errors.add("Career Path is required.")
        if (doctorFocus.isBlank()) errors.add("Focus is required.")
        if (selectedHospitals.isEmpty()) errors.add("Hospital is required.")
        if (selectedServices.isEmpty()) errors.add("Services is required.")
        if (consultantFee.isBlank()) errors.add("Consultation Fee is required.")
        if (speciality.isBlank()) errors.add("Speciality is required.")
        if (selectedSlots.isEmpty()) errors.add("Slots Are required")

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

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(10.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    TextInputField(
                        value = doctorName,
                        onValueChange = { doctorName = it },
                        label = "Doctor Name",
                        icon = Icons.Default.Person,
                    )

                    TextInputField(
                        value = age,
                        onValueChange = { age = it },
                        label = "Age",
                        icon = Icons.Default.Person,
                    )

                    TextInputField(
                        value = consultantFee,
                        onValueChange = { consultantFee = it },
                        label = "Consultation Fee",
                        icon = Icons.Default.Person,
                    )

                    TextInputField(
                        value = doctorExperience,
                        onValueChange = { doctorExperience = it },
                        label = "Doctor Experience",
                        icon = Icons.Default.Person,
                    )

                    TextInputField(
                        value = speciality,
                        onValueChange = { speciality = it },
                        label = "Doctor Speciality",
                        icon = Icons.Default.Person,
                    )

                    TextInputField(
                        value = doctorProfileText,
                        onValueChange = { doctorProfileText = it },
                        label = "Doctor Profile",
                        icon = Icons.Default.Person,
                        minLines = 3,
                    )

                    TextInputField(
                        value = doctorCareerPath,
                        onValueChange = { doctorCareerPath = it },
                        label = "Career Path",
                        icon = Icons.Default.Person,
                        minLines = 3,
                    )

                    TextInputField(
                        value = doctorFocus,
                        onValueChange = { doctorFocus = it },
                        label = "Doctor Focus",
                        icon = Icons.Default.Person,
                        minLines = 3,
                    )

                    TextInputField(
                        value = selectedHospitals.joinToString(", ") { it.name },
                        onValueChange = { },
                        label = "Hospital",
                        icon = Icons.Default.Person,
                        enabled = false,
                        onClick = { showHospitalList = true }
                    )

                    TextInputField(
                        value = selectedServices.joinToString(", ") { it.name },
                        onValueChange = { },
                        label = "Services",
                        icon = Icons.Default.Person,
                        enabled = false,
                        onClick = { showServiceList = true }
                    )

                    TextInputField(
                        value = selectedSlots.joinToString(", ") { it.name },
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
                            }
                        },
                        errorMessage = { message ->
                            toasterEvent = ToastEvent(message)
                        })

                    if (doctorSubmissionState == DoctorSubmissionState.Loading) {
                        CircularProgressIndicator()
                    } else {
                        GradientButton(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                if (validateForm()) {
                                    scope.launch {
                                        imageFile1?.let { doctorImage ->
                                            viewModal.addDoctor(
                                                doctorsMaster = DoctorMaster(
                                                    id = "",
                                                    name = doctorName,
                                                    experience = doctorExperience,
                                                    profilePic = "",
                                                    age = age,
                                                    services = selectedServices.map { it.id },
                                                    hospital = selectedHospitals.map { it.id },
                                                    consltFee = consultantFee,
                                                    reviews = "",
                                                    careerPath = doctorCareerPath,
                                                    focus = doctorFocus,
                                                    profile = doctorProfileText,
                                                    slots = selectedSlots.map { it.id },
                                                    updatedAt = getCurrentTimeStamp(),
                                                    createdAt = getCurrentTimeStamp()
                                                ),
                                                file = doctorImage
                                            )
                                        }
                                    }
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