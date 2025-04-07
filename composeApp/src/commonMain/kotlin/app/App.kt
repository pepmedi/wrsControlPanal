package app

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import dashboard.DashboardApp
import doctor.presentation.DoctorListScreenRoot
import org.jetbrains.compose.ui.tooling.preview.Preview
import controlPanalUser.domain.UserRole

@Composable
@Preview
fun App(prefs:DataStore<Preferences>) {
    MaterialTheme {
        val navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = Route.DashboardGraph
        ){
            navigation<Route.DashboardGraph>(
                startDestination = Route.DashboardRoute
            ){
                composable<Route.DashboardRoute>(
                    exitTransition = { slideOutHorizontally() },
                    popEnterTransition = { slideInHorizontally() }
                ) {
//                    DashboardApp(UserRole.ADMIN, onLogout = {})
                }

                composable<Route.DoctorListScreenRoot>(
                    exitTransition = { slideOutHorizontally() },
                    popEnterTransition = { slideInHorizontally() }
                ) {
                    DoctorListScreenRoot(onDoctorClick = {})
                }

//                composable<Route.AddDoctorScreenRoute>(
//                    exitTransition = { slideOutHorizontally() },
//                    popEnterTransition = { slideInHorizontally() }
//                ) {
//                    AddDoctorScreen()
//                }
            }
        }
    }
}