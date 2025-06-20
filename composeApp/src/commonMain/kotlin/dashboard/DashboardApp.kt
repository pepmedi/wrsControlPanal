package dashboard

import PrimaryAppColor
import SidebarItem
import achievements.screen.AllAchievementsScreenRoot
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import appointment.presentation.AppointmentsScreenRoot
import blog.screen.BlogListScreenRoot
import controlPanalUser.domain.UserRole
import controlPanalUser.domain.UserSession
import controlPanalUser.presentation.PanelUserScreenRoot
import doctor.screen.DoctorListScreenRoot
import hospital.presentation.AllHospitalListScreenRoot
import services.presentation.AllServicesListScreenRoot
import updateInfo.presentation.UpdateInfoScreen

@Composable
fun DashboardApp(userSession: UserSession, userRole: UserRole, onLogout: () -> Unit) {
    var selectedItem by remember { mutableStateOf(SidebarItem.DASHBOARD) }
    MaterialTheme {
        Row(modifier = Modifier.fillMaxSize()) {
            // Show "Dashboard" only if the user is ADMIN
            if (userRole == UserRole.ADMIN) {
                // Add the "Dashboard" option only if user is ADMIN
                Sidebar(
                    items = SidebarItem.entries,
                    onItemClick = { selectedItem = it },
                    selectedItem = selectedItem.title,
                )
            } else {
//                // Define sidebar items based on the user's permissions
                val sidebarItems = buildList {
                    userSession.permissions?.let {
                        if (it.contains("Access Dashboard")) add(SidebarItem.DASHBOARD)
//                        if (it.contains("Update Info")) add(SidebarItem.UPDATE_INFO)
                        if (it.contains("C-Panel Users")) add(SidebarItem.CPANEL_USERS)
                    }
                    add(SidebarItem.APPOINTMENTS)
                    add(SidebarItem.LOGOUT)
                }
//
//                // For Employee, skip Dashboard or customize Sidebar
                Sidebar(
                    items = sidebarItems,
                    onItemClick = { selectedItem = it },
                    selectedItem = selectedItem.title,
                )
            }

            Column(
                modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))
            ) {
                TopBar(title = selectedItem.title)
                when (selectedItem) {
                    SidebarItem.DASHBOARD -> DashboardScreenUi()
//                    SidebarItem.UPDATE_INFO -> UpdateInfoScreen()
                    SidebarItem.APPOINTMENTS -> AppointmentsScreenRoot()
                    SidebarItem.CPANEL_USERS -> PanelUserScreenRoot()
                    SidebarItem.DOCTORS -> DoctorListScreenRoot()

                    SidebarItem.BLOGS -> BlogListScreenRoot()
                    SidebarItem.SERVICES -> AllServicesListScreenRoot()
                    SidebarItem.HOSPITALS -> AllHospitalListScreenRoot()

                    SidebarItem.ACHIEVEMENTS -> AllAchievementsScreenRoot()

                    SidebarItem.LOGOUT -> {
                        onLogout()
                    }

                    else -> DashboardScreenUi()
                }
            }
        }
    }
}


@Composable
fun Sidebar(items: List<SidebarItem>, selectedItem: String, onItemClick: (SidebarItem) -> Unit) {
    val columnState = rememberScrollState()
    Column(
        modifier = Modifier.width(150.dp)
            .fillMaxHeight()
            .background(PrimaryAppColor)
            .verticalScroll(columnState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        items.forEach { item ->
            TextButton(
                onClick = { onItemClick(item) }, modifier = Modifier
                    .shadow(
                        elevation = 2.dp,
                        shape = RoundedCornerShape(3.dp),
                        ambientColor = Color.Yellow
                    )
                    .padding(5.dp)
                    .fillMaxWidth()
                    .background(if (item.title == selectedItem) Color.White else Color.Transparent)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.title,
                        color = if (item.title == selectedItem) PrimaryAppColor else Color.White
                    )

                    // Show logout icon next to "Log out"
                    if (item.title == "Log out") {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp, // Use a built-in logout icon
                            contentDescription = "Logout",
                            tint = if (item.title == selectedItem) PrimaryAppColor else Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TopBar(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth().height(40.dp).background(PrimaryAppColor),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, color = Color.White)
    }
}
