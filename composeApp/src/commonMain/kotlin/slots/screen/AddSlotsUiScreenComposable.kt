package slots.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import doctor.presentation.components.TextInputField
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

    MaterialTheme {
        Scaffold(containerColor = Color.White) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                TextInputField(
                    value = slotsName,
                    onValueChange = { slotsName = it },
                    label = "Service Name",
                    icon = Icons.Outlined.Home
                )

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                } else {
                    Button(modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(5.dp),
                        onClick = {
                            isLoading = true
                            scope.launch {
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
                                            onBackClick()
                                        } else {
                                            isLoading = false
                                        }
                                    }
                            }

                        }
                    ) {
                        Text("Submit")
                    }
                }
            }
        }
    }
}

