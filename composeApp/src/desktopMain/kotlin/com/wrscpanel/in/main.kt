package com.wrscpanel.`in`

import PrimaryAppColor
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dashboard.DashboardScreenUi
import user.UserRole


@Composable
//fun DashboardApp(userMaster: UserMasterControlPanel, userRole: UserRole, onLogout: () -> Unit) {
fun DashboardApp(userRole: UserRole, onLogout: () -> Unit) {
    var selectedItem by remember { mutableStateOf("Dashboard") }
    MaterialTheme {
        Row(modifier = Modifier.fillMaxSize()) {
            // Show "Dashboard" only if the user is ADMIN
//            if (userRole == UserRole.ADMIN) {
                // Add the "Dashboard" option only if user is ADMIN
                Sidebar(
                    items = listOf(
                        "Dashboard",
                        "Analytics",
                        "Shop",
                        "Product",
                        "Shop Video",
                        "Tickets",
                        "Banner",
                        "C-Panel Users",
                        "Application Users",
                        "Shop Approval",
                        "Product Approval",
                        "Shop Video Approval",
                        "Log out"
                    ),
                    onItemClick = { selectedItem = it },
                    selectedItem = selectedItem,
                )
//            } else {
//                // Define sidebar items based on the user's permissions
//                val sidebarItems = buildList {
//                    if (userMaster.permissions.contains("Access Dashboard")) add("Dashboard")
//                    if (userMaster.permissions.contains("View Analytics")) add("Analytics")
//                    if (userMaster.permissions.contains("Access Shops")) add("Shop")
//                    if (userMaster.permissions.contains("Access Products")) add("Product")
//                    if (userMaster.permissions.contains("Access Shop Videos")) add("Shop Video")
//                    if (userMaster.permissions.contains("View Tickets")) add("Tickets")
//                    if (userMaster.permissions.contains("Manage Banners")) add("Banner")
//                    if (userMaster.permissions.contains("View Users")) add("C-Panel Users")
//                    if (userMaster.permissions.contains("Shop Approval")) add("Shop Approval")
//                    if (userMaster.permissions.contains("Product Approval")) add("Product Approval")
//                    if (userMaster.permissions.contains("Shop Video Approval")) add("Shop Video Approval")
//                    if (userMaster.permissions.contains("App Users")) add("Application Users")
//                    add("Log out")
//                }
//
//                // For Employee, skip Dashboard or customize Sidebar
//                Sidebar(
//                    items = sidebarItems,
//                    onItemClick = { selectedItem = it },
//                    selectedItem = selectedItem,
//                )
//            }

            Column(
                modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))
            ) {
                TopBar(title = selectedItem)
                when (selectedItem) {
                    "Dashboard" -> DashboardScreenUi()
//                    "Analytics" -> AnalyticsScreenUi()
//                    "Shop" -> ShopScreen(userMaster, userRole)
//                    "Product" -> ProductScreen(userMaster, userRole)
//                    "Shop Video" -> ShopVideosScreenUi(userRole)
//                    "Banner" -> BannerScreen()
//                    "C-Panel Users" -> UsersProfileScreen()
//                    "Shop Approval" -> ShopApprovalScreenUi()
//                    "Product Approval" -> ProductApprovalScreenUi()
//                    "Shop Video Approval" -> ShopVideoApprovalScreen()
//                    "Tickets" -> TicketScreenUi()
//                    "Application Users" -> AppUsersScreen(userMaster, userRole)
//                    "Log out" -> {
//                        onLogout()
//                    }

                    else -> DashboardScreenUi()
                }
            }
        }
    }
}

@Composable
fun Sidebar(items: List<String>, selectedItem: String, onItemClick: (String) -> Unit) {
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
            TextButton(onClick = { onItemClick(item) }, modifier = Modifier
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(3.dp), ambientColor = Color.Yellow)
                .padding(5.dp)
                .fillMaxWidth()
                .background(if (item == selectedItem) Color.White else Color.Transparent)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item,
                        color = if (item == selectedItem) PrimaryAppColor else Color.White
                    )

                    // Show logout icon next to "Log out"
                    if (item == "Log out") {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp, // Use a built-in logout icon
                            contentDescription = "Logout",
                            tint = if (item == selectedItem) PrimaryAppColor else Color.White
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

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "We Are Spine Control Panel",
    ) {
        var isLoggedIn by remember { mutableStateOf(false) } // Track login state
        var isAdmin by remember { mutableStateOf(UserRole.EMPLOYEE) }
//        var userMaster by remember { mutableStateOf(UserMasterControlPanel()) }

//        if (isLoggedIn) {
            // Show the Dashboard after successful login
            DashboardApp( isAdmin) {
                isLoggedIn = false
                isAdmin = UserRole.EMPLOYEE
//                userMaster = UserMasterControlPanel()
            }
//        } else {
//            // Show the Login Screen
//            LoginScreen { status, _, role, user ->
//                // Perform login validation here if needed
//                isLoggedIn = status
//                isAdmin = role
//                userMaster = user
//            }
//        }
    }
}