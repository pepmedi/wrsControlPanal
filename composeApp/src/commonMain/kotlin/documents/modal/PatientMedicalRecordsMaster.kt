package documents.modal

import kotlinx.serialization.Serializable

@Serializable
data class PatientMedicalRecordsMaster(
    val id: String = "",
    val appointmentId: String= "",
    val recordName: String= "",
    val description: String= "",
    val fileUrl: String= "",
    val mimeType: String= "",
    val isActive: String = "", // 0->active , 1-> inactive
    val storagePath: String= "",
    val createdAt: String= "",
)
