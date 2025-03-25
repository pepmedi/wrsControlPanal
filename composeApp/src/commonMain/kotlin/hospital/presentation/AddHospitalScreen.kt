package hospital.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import core.CancelButton
import doctor.presentation.components.TextInputField
import hospital.domain.HospitalMaster
import hospital.domain.HospitalStates
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import util.getCurrentTimeStamp

@Composable
fun AddHospitalScreen(viewModal: HospitalViewModal = koinViewModel(), onBackClick: () -> Unit) {

    val hospitalStates by viewModal.hospitalStates.collectAsStateWithLifecycle()

    var hospitalName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    // Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(hospitalStates) {
        if (hospitalStates is HospitalStates.Success) {
            viewModal.resetSubmissionState() // Reset state
            onBackClick() // Navigate back
        }
    }

    fun validateForm(): Boolean {
        val errors = mutableListOf<String>()

        if (hospitalName.isBlank()) errors.add("Hospital Name is required")
        if (address.isBlank()) errors.add("Address is required")

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
                            value = hospitalName,
                            onValueChange = { hospitalName = it },
                            label = "Hospital Name",
                            icon = Icons.Outlined.Home
                        )
                    }

                    item {
                        TextInputField(
                            value = address,
                            onValueChange = { address = it },
                            label = "Hospital Address",
                            icon = Icons.Outlined.LocationOn
                        )
                    }

                    item {
                        if (hospitalStates == HospitalStates.Loading) {
                            CircularProgressIndicator()
                        } else {
                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(5.dp),
                                onClick = {
                                    if (validateForm()) {
                                        scope.launch {
                                            viewModal.addHospital(
                                                hospitalMaster = HospitalMaster(
                                                    id = "",
                                                    name = hospitalName,
                                                    address = address,
                                                    createdAt = getCurrentTimeStamp(),
                                                    updatedAt = getCurrentTimeStamp()
                                                )
                                            )
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