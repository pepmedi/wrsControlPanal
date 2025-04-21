package slots.domain

import kotlinx.serialization.Serializable


@Serializable
data class SlotsMaster(
    var id: String,
    val name: String,
    val createdAt: String,
    val updatedAt: String
)
