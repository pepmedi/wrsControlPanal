package controlPanalUser.presentation

import PrimaryAppColor
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dokar.sonner.TextToastAction
import com.dokar.sonner.ToastType
import com.dokar.sonner.Toaster
import com.dokar.sonner.rememberToasterState
import component.GradientButton
import controlPanalUser.domain.UserMasterControlPanel
import controlPanalUser.domain.UserRole
import controlPanalUser.presentation.component.DoctorListDialog
import controlPanalUser.viewModel.PanelUserUpdateScreenAction
import controlPanalUser.viewModel.UpdatePanelUserUiState
import controlPanalUser.viewModel.UpdatePanelUserViewModel
import core.CancelButton
import doctor.screen.components.TextInputField
import org.koin.compose.viewmodel.koinViewModel
import util.ToastEvent
import util.Util.toNameFormat

@Composable
fun UpdatePanelUserScreenRoot(
    viewModel: UpdatePanelUserViewModel = koinViewModel(),
    currentUser: UserMasterControlPanel,
    onBackClick: () -> Unit,
    onUpdate: (UserMasterControlPanel?) -> Unit
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    val toaster = rememberToasterState { }
    var toasterEvent by remember { mutableStateOf<ToastEvent?>(null) }

    LaunchedEffect(currentUser) {
        viewModel.onAction(PanelUserUpdateScreenAction.OnCurrentUserReceived(currentUser))
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

    LaunchedEffect(uiState.isSuccess, uiState.isError) {
        if (uiState.isSuccess) {
            toaster.show(
                message = "Doctor Data updated successfully",
                type = ToastType.Success
            )
            onUpdate(uiState.updatedUser)
            viewModel.resetData()
            onBackClick()
        } else if (uiState.isError.isNotEmpty()) {
            toasterEvent = ToastEvent(message = "SomeThing went wrong.\n please try again ")
        }
    }

    UpdatePanelUserScreen(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                is PanelUserUpdateScreenAction.OnBackButtonClicked -> onBackClick()
                is PanelUserUpdateScreenAction.OnUpdateUserButtonClicked -> {
                    if (!uiState.isFormValid) {
                        toasterEvent = ToastEvent(uiState.getErrorMessage())
                    }
                }

                else -> Unit
            }
            viewModel.onAction(action)
        })

    Toaster(
        state = toaster,
        richColors = true,
        alignment = Alignment.TopEnd
    )
}

@Composable
fun UpdatePanelUserScreen(
    uiState: UpdatePanelUserUiState,
    onAction: (PanelUserUpdateScreenAction) -> Unit
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
                                        PanelUserUpdateScreenAction.OnUserNameChanged(
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
                                        PanelUserUpdateScreenAction.OnUserPassChanged(
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
                                    PanelUserUpdateScreenAction.OnIsActiveChanged(
                                        it
                                    )
                                )
                            }

                        }

                        item {
                            UserRoleSelector(
                                uiState.empType,
                                onRoleSelected = {
                                    val empType = when (it) {
                                        UserRole.ADMIN -> "0"
                                        UserRole.DOCTOR -> "1"
                                        UserRole.EMPLOYEE -> "2"
                                    }
                                    onAction(PanelUserUpdateScreenAction.OnUserRoleChanged(empType))
                                })
                        }

                        if (uiState.empType == "1" || uiState.empType == "2") {
                            item {
                                Text(text = "Add Doctor")

                                TextInputField(
                                    value = uiState.selectedDoctor.name.toNameFormat(),
                                    onValueChange = {
                                        onAction(
                                            PanelUserUpdateScreenAction.OnUserPassChanged(
                                                it
                                            )
                                        )
                                    },
                                    label = "Select Doctor",
                                    icon = Icons.Default.Person,
                                    enabled = false,
                                    onClick = {
                                        onAction(
                                            PanelUserUpdateScreenAction.OnShowDoctorListClicked(
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
                                ) {
                                    onAction(
                                        PanelUserUpdateScreenAction.OnUserPermissionsChanged(
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
                                    text = "Update",
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = {
                                        onAction(PanelUserUpdateScreenAction.OnUpdateUserButtonClicked)
                                    })
                            }
                        }

                        item {
                            CancelButton(onBackClick = { onAction(PanelUserUpdateScreenAction.OnBackButtonClicked) })
                        }
                    }
                }

                if (uiState.showDoctorList) {
                    DoctorListDialog(
                        doctorList = uiState.doctorList,
                        onDismiss = {
                            onAction(
                                PanelUserUpdateScreenAction.OnShowDoctorListClicked(
                                    false
                                )
                            )
                        },
                        onSubmit = { onAction(PanelUserUpdateScreenAction.OnSelectedDoctorChanged(it)) })
                }
            }
        }
    }
}