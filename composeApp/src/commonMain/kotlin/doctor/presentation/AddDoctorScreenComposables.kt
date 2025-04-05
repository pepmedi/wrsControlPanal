package doctor.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import core.domain.DataError
import core.domain.Result
import doctor.domain.DoctorSubmissionState
import doctor.domain.DoctorsMaster
import doctor.presentation.components.TextInputField
import hospital.domain.HospitalMaster
import hospital.presentation.components.HospitalListDialog
import hospital.presentation.components.ServicesListDialog
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import services.domain.ServicesMaster
import util.FileCompressor
import util.FileUtil.loadAndCompressImage
import util.getCurrentTimeStamp
import java.io.File

@Composable
fun AddDoctorScreen(viewModal: DoctorViewModal = koinViewModel(), onBackClick: () -> Unit) {

    val doctorSubmissionState by viewModal.doctorSubmissionState.collectAsStateWithLifecycle()
    val hospitalListState = viewModal.hospitalList.collectAsStateWithLifecycle()
    val servicesListState = viewModal.servicesList.collectAsStateWithLifecycle()

    var doctorName by remember { mutableStateOf("") }
    var doctorExperience by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }

    var consultantFee by remember { mutableStateOf("") }
    var review by remember { mutableStateOf("") }
    var speciality by remember { mutableStateOf("") }
    var imageBitmap1 by remember { mutableStateOf<ImageBitmap?>(null) }
    var imageFile1 by remember { mutableStateOf<File?>(null) }
    var snackBarMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    var showHospitalList by remember { mutableStateOf(false) }
    var selectedHospitals by remember { mutableStateOf(emptyList<HospitalMaster>()) }

    var showServiceList by remember { mutableStateOf(false) }
    var selectedServices by remember { mutableStateOf(emptyList<ServicesMaster>()) }

    // Snackbar
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(doctorSubmissionState) {
        if (doctorSubmissionState is DoctorSubmissionState.Success) {
            viewModal.resetSubmissionState() // Reset state
            onBackClick() // Navigate back
        }
    }
    fun validateForm(): Boolean {
        val errors = mutableListOf<String>()

        if (doctorName.isBlank()) errors.add("Doctor Name is required.")
        if (doctorExperience.isBlank()) errors.add("Experience is required.")
        if (age.isBlank()) errors.add("Age is required.")
        if (selectedHospitals.isEmpty()) errors.add("Hospital is required.")
        if (selectedServices.isEmpty()) errors.add("Services is required.")
        if (consultantFee.isBlank()) errors.add("Consultation Fee is required.")
        if (review.isBlank()) errors.add("Review is required.")
        if (speciality.isBlank()) errors.add("Speciality is required.")

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
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
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
                            value = doctorExperience,
                            onValueChange = { doctorExperience = it },
                            label = "Doctor Experience",
                            icon = Icons.Default.Person,
                        )
                    }

                    item {
                        TextInputField(
                            value = age,
                            onValueChange = { age = it },
                            label = "Age",
                            icon = Icons.Default.Person,
                        )
                    }

                    item {
                        TextInputField(
                            value = selectedHospitals.joinToString(", ") { it.name },
                            onValueChange = { },
                            label = "Hospital",
                            icon = Icons.Default.Person,
                            enabled = false,
                            onClick = { showHospitalList = true }
                        )
                    }

                    item {
                        TextInputField(
                            value = selectedServices.joinToString(", ") { it.name },
                            onValueChange = { },
                            label = "Services",
                            icon = Icons.Default.Person,
                            enabled = false,
                            onClick = { showServiceList = true }
                        )
                    }

                    item {
                        TextInputField(
                            value = consultantFee,
                            onValueChange = { consultantFee = it },
                            label = "Consultation Fee",
                            icon = Icons.Default.Person,
                        )
                    }

                    item {
                        TextInputField(
                            value = review,
                            onValueChange = { review = it },
                            label = "Doctor Experience",
                            icon = Icons.Default.Person,
                        )
                    }

                    item {
                        TextInputField(
                            value = speciality,
                            onValueChange = { speciality = it },
                            label = "Doctor Experience",
                            icon = Icons.Default.Person,
                        )
                    }

                    item {
                        //first image
                        ImageSelector(
                            imageBitmap = imageBitmap1,
                            onImageSelected = { file ->
                                scope.launch {
                                    imageFile1 = FileCompressor.loadAndCompressImage(file)
                                    imageBitmap1 = loadAndCompressImage(file)
                                }
                            },
                            snackBarMessage = { message ->
                                snackBarMessage = message
                            })
                    }

                    item {
                        if (doctorSubmissionState == DoctorSubmissionState.Loading) {
                            CircularProgressIndicator()
                        } else {
                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(5.dp),
                                onClick = {
                                    if (validateForm()) {
                                        scope.launch {
                                            imageFile1?.let {
                                                viewModal.addDoctor(
                                                    doctorsMaster = DoctorsMaster(
                                                        id = "",
                                                        name = doctorName,
                                                        experience = doctorExperience,
                                                        profilePic = "",
                                                        age = age,
                                                        services = selectedServices.map { it.id },
                                                        hospital = selectedHospitals.map { it.id },
                                                        consltFee = consultantFee,
                                                        reviews = review,
                                                        updatedAt = getCurrentTimeStamp(),
                                                        createdAt = getCurrentTimeStamp()
                                                    ),
                                                    file = it
                                                )
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
                if (showHospitalList) {
                    HospitalListDialog(
                        hospitalList = hospitalListState.value,
                        onDismiss = { showHospitalList = false },
                        onSubmit = { selectedHospitals = it }
                    )
                }

                if (showServiceList) {
                    ServicesListDialog(
                        serviceList = servicesListState.value,
                        onDismiss = { showServiceList = false },
                        onSubmit = {
                            selectedServices = it
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DoctorListScreenRoot(
    viewModal: DoctorViewModal = koinViewModel(),
    onDoctorClick: (DoctorsMaster) -> Unit
) {
    val list = viewModal.doctorList.collectAsStateWithLifecycle()
    DoctorListScreen(list.value, onDoctorClick = {
        onDoctorClick(it)
        println(it.name)
    })
}

@Composable
fun DoctorListScreen(
    doctorState: Result<List<DoctorsMaster>, DataError.Remote>,
    onDoctorClick: (DoctorsMaster) -> Unit
) {

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Doctors List",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        when (doctorState) {
            is Result.Success -> {
                LazyColumn {
                    items(doctorState.data) { doctor ->
                        DoctorItem(doctor, onDoctorClick = {
                            onDoctorClick(it)
                        })
                    }
                }
            }

            is Result.Error -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        }
    }
}

@Composable
fun DoctorItem(doctor: DoctorsMaster, onDoctorClick: (DoctorsMaster) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp).clickable { onDoctorClick(doctor) }) {
            Text(text = "Name: ${doctor.name}", style = MaterialTheme.typography.bodyLarge)
            Text(text = "Age: ${doctor.age}", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "Address: ${doctor.experience}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}