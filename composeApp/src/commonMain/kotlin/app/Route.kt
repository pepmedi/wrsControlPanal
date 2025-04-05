package app

import kotlinx.serialization.Serializable

sealed interface Route {

    @Serializable
    data object DashboardGraph : Route

    @Serializable
    data object DashboardRoute : Route

//    @Serializable
//    data object AddDoctorScreenRoute:Route

    @Serializable
    data object DoctorListScreenRoot : Route

    @Serializable
    data object CreatePanelUserScreenRoute : Route
}