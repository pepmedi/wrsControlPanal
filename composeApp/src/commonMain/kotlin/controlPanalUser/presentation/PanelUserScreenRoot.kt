package controlPanalUser.presentation

import Java20
import PrimaryAppColor
import SecondaryAppColor
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.ripple
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import component.SlideInScreen
import controlPanalUser.domain.UserMasterControlPanel
import controlPanalUser.domain.UserRole
import controlPanalUser.viewModel.PanelUserScreenViewModel
import controlPanalUser.viewModel.PanelUserUiEvent
import controlPanalUser.viewModel.PanelUserUiState
import org.koin.compose.viewmodel.koinViewModel
import util.Util.toNameFormat

@Composable
fun PanelUserScreenRoot(viewModel: PanelUserScreenViewModel = koinViewModel()) {

    val uiState by viewModel.state.collectAsStateWithLifecycle()

    PanelUserScreen(
        uiState = uiState,
        onAction = { action ->
            viewModel.onAction(action)
        })
}

@Composable
fun PanelUserScreen(
    uiState: PanelUserUiState,
    onAction: (PanelUserUiEvent) -> Unit
) {
    var expandedCardId by remember { mutableStateOf<String?>(null) }
    var showCreateUserUI by remember { mutableStateOf(false) }
    var showUpdateUserUI by remember { mutableStateOf(false) }
    var currentUserForUpdate by remember { mutableStateOf(UserMasterControlPanel()) }

    MaterialTheme {
        Scaffold(containerColor = Java20) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    if (uiState.isLoading) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                                color = SecondaryAppColor
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                        ) {
                            items(items = uiState.users) { users ->
                                UsersItemCard(
                                    userMaster = users,
                                    modifier = Modifier.animateItem(),
                                    isExpanded = expandedCardId == users.id,
                                    onExpand = { expandedCardId = users.id },
                                    onCollapse = { expandedCardId = null },
                                    onUpdateUserClick = {
                                        currentUserForUpdate = users
                                        showUpdateUserUI = it
                                        expandedCardId = null
                                    },
                                    onErrorMessage = { },
                                    onSnackBarMessage = {},
                                    currentUser = { },
                                    onDeleteUserClick = {
                                        onAction(PanelUserUiEvent.DeleteUser(users))
                                        expandedCardId = null
                                    },
                                    onUserBlockedClick = {
                                        onAction(PanelUserUiEvent.ChangeUserStatus(users))
                                        expandedCardId = null
                                    }
                                )
                            }
                        }
                    }
                }

                FloatingActionButton(
                    onClick = {
                        showCreateUserUI = true
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    shape = RoundedCornerShape(corner = CornerSize(8.dp)),
                    containerColor = PrimaryAppColor
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = "Add Product")
                }

                SlideInScreen(showCreateUserUI) {
                    PanelUserCreationScreenRoot(onBackClick = {
                        showCreateUserUI = false
                    }, onSuccessful = {
                        onAction(PanelUserUiEvent.UserCreated(it))
                    })
                }

                SlideInScreen(showUpdateUserUI) {
                    UpdatePanelUserScreenRoot(
                        onBackClick = {
                            showUpdateUserUI = false
                        }, onUpdate = {
                            onAction(PanelUserUiEvent.UpdateUser(it))
                        }, currentUser = currentUserForUpdate
                    )
                }
            }
        }
    }
}

@Composable
fun UsersItemCard(
    userMaster: UserMasterControlPanel,
    modifier: Modifier,
    isExpanded: Boolean,
    onExpand: () -> Unit,
    onCollapse: () -> Unit,
    onUpdateUserClick: (Boolean) -> Unit,
    onErrorMessage: (String) -> Unit,
    onSnackBarMessage: (String) -> Unit,
    currentUser: (String) -> Unit,
    onDeleteUserClick: () -> Unit,
    onUserBlockedClick: () -> Unit
) {
    val isUserBlocked = userMaster.isActive == "1"
    val backgroundColor = if (isUserBlocked) Color.LightGray else Color.White
    val userRole = when (userMaster.empType) {
        "0" -> UserRole.ADMIN
        "1" -> UserRole.DOCTOR
        "2" -> UserRole.EMPLOYEE
        else -> UserRole.ADMIN
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color.LightGray)
            ) { if (isExpanded) onCollapse() else onExpand() },
        backgroundColor = backgroundColor,
        elevation = 4.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .animateContentSize()
        ) {
            Text(
                text = userMaster.userName.toNameFormat(),
                style = MaterialTheme.typography.headlineLarge,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = userRole.name.uppercase(),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )

            // If the card is expanded, show the additional buttons
            if (isExpanded) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Button(
                        onClick = {
                            onUserBlockedClick()
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = if (isUserBlocked) Color.Gray else Color.Red)
                    ) {
                        Text(
                            text = if (isUserBlocked) "UnBlock" else "Block",
                            color = Color.White
                        )
                    }

                    Button(
                        onClick = {
                            onUpdateUserClick(true)
                            currentUser(userMaster.id)
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Green)
                    ) {
                        Text(text = "Update", color = Color.White)
                    }

                    Button(
                        onClick = {
                            onDeleteUserClick()
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray)
                    ) {
                        Text(text = "Delete", color = Color.Black)
                    }
                }
            }
        }
    }
}