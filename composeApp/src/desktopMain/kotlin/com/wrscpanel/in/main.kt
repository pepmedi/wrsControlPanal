package com.wrscpanel.`in`

import DATA_STORE_FILE_NAME
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import app.App
import com.plcoding.bookpedia.di.initKoin
import createDataStore

//fun DashboardApp(userMaster: UserMasterControlPanel, userRole: UserRole, onLogout: () -> Unit) {


fun main() {
    val prefs = createDataStore {
        DATA_STORE_FILE_NAME
    }
    initKoin()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "We Are Spine Control Panel",
        ) {
            App(
                prefs = prefs
            )
//        var isLoggedIn by remember { mutableStateOf(false) } // Track login state
//        var isAdmin by remember { mutableStateOf(UserRole.EMPLOYEE) }
////        var userMaster by remember { mutableStateOf(UserMasterControlPanel()) }
//
////        if (isLoggedIn) {
//            // Show the Dashboard after successful login
//            DashboardApp( isAdmin) {
//                isLoggedIn = false
//                isAdmin = UserRole.EMPLOYEE
////                userMaster = UserMasterControlPanel()
//            }
////        } else {
////            // Show the Login Screen
////            LoginScreen { status, _, role, user ->
////                // Perform login validation here if needed
////                isLoggedIn = status
////                isAdmin = role
////                userMaster = user
////            }
////        }
        }
    }
}