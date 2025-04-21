package hospital.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dokar.sonner.TextToastAction
import com.dokar.sonner.ToastType
import com.dokar.sonner.Toaster
import com.dokar.sonner.rememberToasterState
import component.GradientButton
import core.CancelButton
import doctor.screen.components.TextInputField
import hospital.domain.HospitalMaster
import hospital.domain.HospitalStates
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import util.ToastEvent
import util.getCurrentTimeStamp

@Composable
fun AddHospitalScreen(viewModal: HospitalViewModel = koinViewModel(), onBackClick: () -> Unit) {

    val hospitalStates by viewModal.hospitalStates.collectAsStateWithLifecycle()

    var hospitalName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    val toaster = rememberToasterState()
    var toasterEvent by remember { mutableStateOf<ToastEvent?>(null) }

    LaunchedEffect(hospitalStates) {
        if (hospitalStates is HospitalStates.Success) {
            viewModal.resetSubmissionState()
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

                if (hospitalStates == HospitalStates.Loading) {
                    CircularProgressIndicator()
                } else {
                    GradientButton(
                        modifier = Modifier.fillMaxWidth(),
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