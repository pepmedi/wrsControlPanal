package hospital.domain

import okhttp3.Address

data class HospitalMaster(
    val id:String,
    val name:String,
    val address: String,
    val createdAt:String,
    val updatedAt:String
)
