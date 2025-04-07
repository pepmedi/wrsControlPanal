package appUsers

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String = "",
    val name: String = "",
    val mobileNo: String = "",
    val email: String = "",
    val dob: String = "",
    val gender: String = "",
    val password: String = "",
    val createdAt: String = "",
    val updatedAt: String = ""
)
