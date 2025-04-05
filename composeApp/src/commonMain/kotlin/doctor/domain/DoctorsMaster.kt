package doctor.domain

import kotlinx.serialization.Serializable

@Serializable
data class DoctorsMaster(
    val id:String = "",
    val name:String= "",
    val experience:String= "",
    val profilePic:String= "",
    val age:String= "",
    val hospital:List<String> = emptyList(),
    val services:List<String> = emptyList(),
    val slots:List<String> = emptyList(),
    val consltFee:String = "",
    val speciality:String = "",
    val reviews:String= "",
    val createdAt:String= "",
    val updatedAt:String= ""
)
