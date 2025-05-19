package controlPanalUser.domain

import doctor.domain.DoctorMaster

data class PanelUserCreationUiState(
    val isActive: Boolean = false,
    val empType: String = "",
    val userPass: String = "",
    val userName: String = "",
    val selectedDoctor: DoctorMaster = DoctorMaster(),
    val doctorList: List<DoctorMaster> = emptyList(),
    val permissions: Map<String, Boolean> = PanelUserPermissions.defaultPermissions,
    val showDoctorList: Boolean = false,
    val isLoading: Boolean = false,
    val isError: String = "",
    val isSuccess: Boolean = false,
    val addedUser: UserMasterControlPanel? = null
) {
    val isFormValid: Boolean
        get() = userName.isNotBlank()
                && userPass.isNotBlank()
                && empType.isNotBlank()
                && when (empType) {
            "1" -> selectedDoctor.id.isNotBlank() // Doctor needs doctor ID
            "2" -> selectedDoctor.id.isNotBlank() && permissions.any { it.value } // Employee needs doctor ID and permissions
            else -> true // Admin
        }

    fun getErrorMessage(): String {
        return when {
            userName.isBlank() -> "Username cannot be empty"
            userPass.isBlank() -> "Password cannot be empty"
            empType.isBlank() -> "Employee type is required"
            empType == "1" && selectedDoctor.id.isBlank() -> "Doctor must be linked to a valid doctor ID"
            empType == "2" && selectedDoctor.id.isBlank() -> "Employee must be linked to a doctor"
            empType == "2" && permissions.none { it.value } -> "At least one permission must be selected for an employee"
            else -> ""
        }
    }
}