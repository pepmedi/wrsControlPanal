package controlPanalUser.domain

data class UserSession(
    val id: String,
    val userName: String,
    val role: UserRole,
    val linkedDoctorId: String? = null // Only needed for EMPLOYEE
)
