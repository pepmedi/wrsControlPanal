package controlPanalUser.domain

import kotlinx.serialization.Serializable

@Serializable
data class UserMasterControlPanel(
    val id :String = "",
    val userName:String = "",
    val password:String = "",
    var empType:String = "", // 0 -> admin , 1-> doctor , 2-> employee
    var isActive:String = "", // 0-> active , 1-> inActive
    var doctorId:String = "",
    val permissions: Set<String> = emptySet(),
    var createdAt:String = "",
    var updatedAt:String = ""
)
