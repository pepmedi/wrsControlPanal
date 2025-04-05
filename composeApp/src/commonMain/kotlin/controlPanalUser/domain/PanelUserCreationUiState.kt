package controlPanalUser.domain

import doctor.domain.DoctorsMaster

data class PanelUserCreationUiState(
    val isActive:Boolean = false,
    val empType:String = "",
    val userPass:String = "",
    val userName:String = "",
    val selectedDoctor: DoctorsMaster = DoctorsMaster(),
    val doctorList :List<DoctorsMaster> = emptyList(),
    val permissions :Map<String, Boolean> = PanelUserPermissions.defaultPermissions,
    val showDoctorList:Boolean = false,
    val isLoading: Boolean = false,
    val isError: String = "",
    val isSuccess: Boolean = false
)