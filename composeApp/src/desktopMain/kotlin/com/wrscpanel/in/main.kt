package com.wrscpanel.`in`

import DATA_STORE_FILE_NAME
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.plcoding.bookpedia.di.initKoin
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
            var isLoggedIn by remember { mutableStateOf(SessionManager.currentUser != null) }
//
            if (isLoggedIn && SessionManager.currentUser != null) {
                // Show the Dashboard after successful login
                SessionManager.currentUser?.let {
                    DashboardApp(
                        userSession = it,
                        SessionManager.currentUser?.role ?: UserRole.EMPLOYEE,
                        onLogout = {
                            SessionManager.currentUser = null
                            isLoggedIn = false
                        }
                    )
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