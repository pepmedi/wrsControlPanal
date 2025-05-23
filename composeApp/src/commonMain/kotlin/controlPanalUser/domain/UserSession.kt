package controlPanalUser.domain

data class UserSession(
    val id: String,
    val userName: String,
    val role: UserRole,
    val linkedDoctorId: List<String>? = emptyList(),
    val permissions:Set<String>? = null
)
