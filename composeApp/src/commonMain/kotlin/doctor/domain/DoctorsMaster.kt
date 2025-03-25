package doctor.domain

import kotlinx.serialization.Serializable

@Serializable
data class DoctorsMaster(
    val id:String,
    val name:String,
    val experience:String,
    val profilePic:String,
    val age:String,
    val hospital:List<String>,
    val services:List<String>,
    val consltFee:String,
    val reviews:String,
    val createdAt:String,
    val updatedAt:String
)
