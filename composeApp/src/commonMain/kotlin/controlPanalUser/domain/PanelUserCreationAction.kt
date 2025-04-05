package controlPanalUser.domain

import doctor.domain.DoctorsMaster

sealed interface PanelUserCreationAction {
    data class OnUserNameChanged(val userName: String) : PanelUserCreationAction
    data class OnUserPassChanged(val userPass: String) : PanelUserCreationAction
    data class OnUserRoleChanged(val role: String) : PanelUserCreationAction
    data class OnUserPermissionsChanged(val permission: String) : PanelUserCreationAction
    data class OnSelectedDoctorChanged(val doctorsMaster: DoctorsMaster) : PanelUserCreationAction
    data object OnCreateUserButtonClicked : PanelUserCreationAction
    data class OnShowDoctorListClicked(val clicked: Boolean) : PanelUserCreationAction
    data class OnIsActiveChanged(val clicked: Boolean) : PanelUserCreationAction
    data object OnBackButtonClicked : PanelUserCreationAction
}