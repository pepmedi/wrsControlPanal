package blog.domain

import kotlinx.serialization.Serializable

@Serializable
data class BlogMaster(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val createdAt: String = "",
    val updatedAt: String = "",
    val doctorId: String = "",
    val blogActive: String = ""
)

