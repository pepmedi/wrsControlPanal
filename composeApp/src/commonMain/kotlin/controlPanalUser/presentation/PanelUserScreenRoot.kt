package controlPanalUser.presentation

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
import controlPanalUser.domain.UserMasterControlPanel
import controlPanalUser.domain.UserRole
import org.koin.compose.viewmodel.koinViewModel
import util.Util.toNameFormat

@Composable
fun PanelUserScreenRoot(viewModal: PanelUserScreenViewModel = koinViewModel()) {

    val uiState by viewModal.state.collectAsStateWithLifecycle()

    PanelUserScreen(uiState = uiState)
}

@Composable
fun PanelUserScreen(
    uiState: PanelUserUiState
) {
    var expandedCardId by remember { mutableStateOf<String?>(null) }
    var showCreateUserUI by remember { mutableStateOf(false) }

    MaterialTheme {
        Scaffold(containerColor = Color.White) {
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
                                    isExpanded = expandedCardId == users.id,
                                    onExpand = { expandedCardId = users.id },
                                    onCollapse = { expandedCardId = null },
                                    onUpdateUserClick = { },
                                    onErrorMessage = { },
                                    onSnackBarMessage = {},
                                    currentUser = { },
                                    onDeleteUserClick = { status, userId ->

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
                    shape = RoundedCornerShape(corner = CornerSize(8.dp))
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = "Add Product")
                }

                if (showCreateUserUI) {
                    PanelUserCreationScreenRoot(onBack = {
                        showCreateUserUI = false
                    })
                }
            }
        }
    }
}

@Composable
fun UsersItemCard(
    userMaster: UserMasterControlPanel,
    isExpanded: Boolean,
    onExpand: () -> Unit,
    onCollapse: () -> Unit,
    onUpdateUserClick: (Boolean) -> Unit,
    onErrorMessage: (String) -> Unit,
    onSnackBarMessage: (String) -> Unit,
    currentUser: (String) -> Unit,
    onDeleteUserClick: (Boolean, String) -> Unit
) {
    val isUserBlocked = remember { mutableStateOf(userMaster.isActive == "1") }
    val backgroundColor = if (isUserBlocked.value) Color.LightGray else Color.White
    val userRole = when (userMaster.empType) {
        "0" -> UserRole.ADMIN
        "1" -> UserRole.DOCTOR
        "2" -> UserRole.EMPLOYEE
        else -> UserRole.ADMIN
    }

    Card(
        modifier = Modifier
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
                            if (isUserBlocked.value) {

                            } else {

                            }
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = if (isUserBlocked.value) Color.Gray else Color.Red)
                    ) {
                        Text(
                            text = if (isUserBlocked.value) "UnBlock" else "Block",
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