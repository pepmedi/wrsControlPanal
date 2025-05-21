package doctor.domain

import kotlinx.serialization.Serializable

@Serializable
data class DoctorMaster(
    val id: String = "",
    val name: String = "",
    val experience: String = "",
    val age: String = "",
    val profilePic: String = "",
    val doctorInfoPic :String = "",
    val hospital: List<String> = emptyList(),
    val services: List<String> = emptyList(),
    val slots: List<String> = emptyList(),
    val qualification: String = "",
    val consltFee: String = "",
    val speciality: String = "",
    val reviews: String = "",
    val focus: String = "",
    val profile: String = "",
    val careerPath: String = "",
    val createdAt: String = "",
    val updatedAt: String = ""
)
