package hospital.presentation.components

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
import hospital.presentation.HospitalActions
import hospital.presentation.HospitalViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import util.ToastEvent
import util.getCurrentTimeStamp

@Composable
fun UpdateHospitalScreen(
    viewModal: HospitalViewModel = koinViewModel(),
    hospitalMaster: HospitalMaster,
    onBackClick: () -> Unit
) {

    val uiState by viewModal.uiState.collectAsStateWithLifecycle()

    var hospitalName by remember { mutableStateOf(hospitalMaster.name) }
    var address by remember { mutableStateOf(hospitalMaster.address) }

    val scope = rememberCoroutineScope()

    val toaster = rememberToasterState()
    var toasterEvent by remember { mutableStateOf<ToastEvent?>(null) }

    LaunchedEffect(uiState.updatedSuccessFully) {
        if (uiState.updatedSuccessFully) {
            viewModal.onAction(HospitalActions.OnUpdatedSuccessfully)
            toaster.show(
                message = "Hospital Updated Successfully",
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

                if (uiState.isLoading) {
                    CircularProgressIndicator()
                } else {
                    GradientButton(
                        text = "Update",
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            if (validateForm()) {
                                viewModal.onAction(
                                    HospitalActions.OnUpdateHospital(
                                        hospitalMaster = HospitalMaster(
                                            id = hospitalMaster.id,
                                            name = hospitalName,
                                            address = address,
                                            createdAt = hospitalMaster.createdAt,
                                            updatedAt = getCurrentTimeStamp()
                                        )
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