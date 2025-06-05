package hospital.domain

import kotlinx.serialization.Serializable

@Serializable
data class HospitalMaster(
    val id: String,
    val name: String,
    val address: String,
    val hospitalLogoUrl: String = "",
    val createdAt: String,
    val updatedAt: String
)