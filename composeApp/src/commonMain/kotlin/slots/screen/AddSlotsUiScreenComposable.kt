package slots.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dokar.sonner.TextToastAction
import com.dokar.sonner.ToastType
import com.dokar.sonner.Toaster
import com.dokar.sonner.rememberToasterState
import component.GradientButton
import core.CancelButton
import doctor.screen.components.TextInputField
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import slots.domain.SlotsMaster
import slots.viewModel.AddSlotsViewModel
import util.getCurrentTimeStamp

@Composable
fun AddSlotsUiScreen(viewModel: AddSlotsViewModel = koinViewModel(), onBackClick: () -> Unit) {

    var slotsName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val toaster = rememberToasterState()

    MaterialTheme {
        Scaffold(containerColor = Color.White) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                TextInputField(
                    value = slotsName,
                    onValueChange = { slotsName = it },
                    label = "Add Slot",
                    icon = Icons.Outlined.Home
                )

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                } else {
                    GradientButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            isLoading = true
                            scope.launch {
                                if (slotsName.isEmpty() && slotsName.isBlank()) {
                                    toaster.show(message = "Slot name is required",
                                        type = ToastType.Error,
                                        action = TextToastAction(
                                            text = "Done",
                                            onClick = { toaster.dismissAll() }
                                        ))
                                    isLoading = false

                                } else {
                                    viewModel.addSlots(
                                        slotsMaster = SlotsMaster(
                                            id = "",
                                            name = slotsName,
                                            updatedAt = getCurrentTimeStamp(),
                                            createdAt = getCurrentTimeStamp()
                                        )
                                    )
                                        .collect { result ->
                                            if (result) {
                                                isLoading = false
                                                toaster.show(
                                                    message = "SLots Added Successfully",
                                                    type = ToastType.Success,
                                                    action = TextToastAction(
                                                        text = "Done",
                                                        onClick = {
                                                            toaster.dismissAll()
                                                        }
                                                    )
                                                )
                                                onBackClick()
                                            } else {
                                                isLoading = false
                                            }
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

