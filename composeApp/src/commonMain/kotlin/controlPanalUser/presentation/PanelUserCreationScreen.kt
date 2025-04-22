package controlPanalUser.presentation

import PrimaryAppColor
import SecondaryAppColor
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dokar.sonner.TextToastAction
import com.dokar.sonner.ToastType
import com.dokar.sonner.Toaster
import com.dokar.sonner.rememberToasterState
import component.GradientButton
import controlPanalUser.domain.PanelUserCreationAction
import controlPanalUser.domain.PanelUserCreationUiState
import controlPanalUser.domain.RoleCode
import controlPanalUser.domain.UserRole
import controlPanalUser.presentation.component.DoctorListDialog
import core.CancelButton
import doctor.screen.components.TextInputField
import org.koin.compose.viewmodel.koinViewModel
import util.ToastEvent
import util.Util.toNameFormat

@Composable
fun PanelUserCreationScreenRoot(
    viewModal: PanelUserCreationViewModel = koinViewModel(),
    onSuccessful: () -> Unit,
    onBackClick: () -> Unit
) {

    val uiState by viewModal.state.collectAsStateWithLifecycle()

    val toaster = rememberToasterState { }
    var toasterEvent by remember { mutableStateOf<ToastEvent?>(null) }

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

    LaunchedEffect(uiState.isSuccess, uiState.isError) {
        if (uiState.isSuccess) {
            toaster.show(
                message = "Doctor Data updated successfully",
                type = ToastType.Success
            )
            onSuccessful()
            viewModal.resetData()
            onBackClick()
        } else if (uiState.isError.isNotEmpty()) {
            toasterEvent = ToastEvent(message = "SomeThing went wrong.\n please try again ")
        }
    }

    PanelUserCreationScreen(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                is PanelUserCreationAction.OnBackButtonClicked -> onBackClick()
                is PanelUserCreationAction.OnCreateUserButtonClicked -> {
                    if (!uiState.isFormValid) {
                        toasterEvent = ToastEvent(uiState.getErrorMessage())
                    }
                }

                else -> Unit
            }
            viewModal.onAction(action)
        })

    Toaster(
        state = toaster,
        richColors = true,
        alignment = Alignment.TopEnd
    )

}

@Composable
fun PanelUserCreationScreen(
    uiState: PanelUserCreationUiState,
    onAction: (PanelUserCreationAction) -> Unit,
) {

    MaterialTheme {
        Scaffold(containerColor = Color.White) { paddingValue ->
            Box(
                modifier = Modifier.fillMaxSize().background(PrimaryAppColor).padding(paddingValue),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    elevation = CardDefaults.cardElevation(8.dp),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .padding(24.dp)
                            .width(600.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item {
                            CreateTitle()
                        }
                        item {
                            TextInputField(
                                value = uiState.userName,
                                onValueChange = {
                                    onAction(
                                        PanelUserCreationAction.OnUserNameChanged(
                                            it
                                        )
                                    )
                                },
                                label = "User Name",
                                icon = Icons.Default.Person,
                            )
                        }

                        item {
                            TextInputField(
                                value = uiState.userPass,
                                onValueChange = {
                                    onAction(
                                        PanelUserCreationAction.OnUserPassChanged(
                                            it
                                        )
                                    )
                                },
                                label = "Password",
                                icon = Icons.Default.Person,
                            )
                        }


                        item {

                            // Employee Switch Section
                            CreateActiveSwitch(uiState.isActive) {
                                onAction(
                                    PanelUserCreationAction.OnIsActiveChanged(
                                        it
                                    )
                                )
                            }

                        }

                        item {
                            UserRoleSelector(uiState.empType,
                                onRoleSelected = {
                                    val empType = when (it) {
                                        UserRole.ADMIN -> "0"
                                        UserRole.DOCTOR -> "1"
                                        UserRole.EMPLOYEE -> "2"
                                    }
                                    onAction(PanelUserCreationAction.OnUserRoleChanged(empType))
                                })
                        }

                        if (uiState.empType == "1" || uiState.empType == "2") {
                            item {
                                Text(text = "Add Doctor")

                                TextInputField(
                                    value = uiState.selectedDoctor.name.toNameFormat(),
                                    onValueChange = {
                                        onAction(
                                            PanelUserCreationAction.OnUserPassChanged(
                                                it
                                            )
                                        )
                                    },
                                    label = "Select Doctor",
                                    icon = Icons.Default.Person,
                                    enabled = false,
                                    onClick = {
                                        onAction(
                                            PanelUserCreationAction.OnShowDoctorListClicked(
                                                true
                                            )
                                        )
                                    }
                                )
                            }
                        }

                        if (uiState.empType == "2") {
                            item {
                                Text(text = "Permissions")
                            }

                            items(
                                uiState.permissions.toList(),
                                key = { it.first }) { (permission, isChecked) ->

                                CreatePermissionRow(
                                    permission,
                                    isChecked
                                ) { isNowChecked ->
                                    onAction(
                                        PanelUserCreationAction.OnUserPermissionsChanged(
                                            permission
                                        )
                                    )
                                }
                            }
                        }

                        item {
                            if (uiState.isLoading) {
                                CircularProgressIndicator()
                            } else {

                                GradientButton(
                                    text = "Submit",
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = {
                                        onAction(PanelUserCreationAction.OnCreateUserButtonClicked)
                                    })
                            }
                        }

                        item {
                            CancelButton(onBackClick = { onAction(PanelUserCreationAction.OnBackButtonClicked) })
                        }
                    }
                }

                if (uiState.showDoctorList) {
                    DoctorListDialog(doctorList = uiState.doctorList,
                        onDismiss = { onAction(PanelUserCreationAction.OnShowDoctorListClicked(false)) },
                        onSubmit = { onAction(PanelUserCreationAction.OnSelectedDoctorChanged(it)) })
                }
            }
        }
    }
}


@Composable
fun CreateActiveSwitch(iaActive: Boolean, onSwitchChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .border(0.5.dp, Color.Black, RoundedCornerShape(2.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Is Active",
            modifier = Modifier.weight(1f).padding(start = 5.dp),
            color = Color.Black,
            style = MaterialTheme.typography.bodyLarge
        )
        Switch(
            checked = iaActive,
            onCheckedChange = onSwitchChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Yellow,
                checkedTrackColor = Color.DarkGray
            ),
            modifier = Modifier.padding(end = 5.dp)
        )
    }
    Spacer(modifier = Modifier.height(10.dp))
}

@Composable
fun CreateTitle() {
    Text(
        text = "Create new User",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = SecondaryAppColor
    )
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
fun UserRoleSelector(
    selectedRole: String,
    onRoleSelected: (UserRole) -> Unit
) {
    val userRole = when (selectedRole) {
        "0" -> UserRole.ADMIN
        "1" -> UserRole.DOCTOR
        "2" -> UserRole.EMPLOYEE
        else -> null
    }

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text(
            text = "Select Role",
            style = MaterialTheme.typography.titleMedium,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        UserRole.entries.forEach { role ->
            Row(
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onRoleSelected(role) }
            ) {
                RadioButton(
                    selected = role == userRole,
                    onClick = { onRoleSelected(role) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = Color.Yellow,
                        unselectedColor = Color.DarkGray
                    )
                )
                Text(
                    text = role.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black
                )
            }
        }
    }
}


@Composable
fun CreatePermissionRow(
    permission: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .border(0.5.dp, Color.Black, RoundedCornerShape(2.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = permission,
            modifier = Modifier.weight(1f).padding(start = 5.dp),
            color = Color.Black,
            style = MaterialTheme.typography.bodyLarge
        )
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Yellow,
                checkedTrackColor = Color.DarkGray
            ),
            modifier = Modifier.padding(end = 5.dp)
        )
    }
}
