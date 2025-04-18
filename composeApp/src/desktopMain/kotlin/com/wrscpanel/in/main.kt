package com.wrscpanel.`in`

import DATA_STORE_FILE_NAME
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import app.App
import com.plcoding.bookpedia.di.initKoin
import controlPanalUser.domain.UserMasterControlPanel
import controlPanalUser.domain.UserRole
import controlPanalUser.repository.SessionManager
import createDataStore
import dashboard.DashboardApp
import login.presentation.LoginScreen

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
//            App(
//                prefs = prefs
//            )
            var isLoggedIn by remember { mutableStateOf(false) } // Track login state
            var userMaster by remember { mutableStateOf(UserMasterControlPanel()) }
//
            if (isLoggedIn) {
                // Show the Dashboard after successful login
                SessionManager.currentUser?.let {
                    DashboardApp(it,SessionManager.currentUser?.role ?: UserRole.EMPLOYEE) {
                        isLoggedIn = false
                        userMaster = UserMasterControlPanel()
                    }
                }
            } else {

                LoginScreen { status ->
                    // Perform login validation here if needed
                    isLoggedIn = status
                }
            }
        }
    }
}