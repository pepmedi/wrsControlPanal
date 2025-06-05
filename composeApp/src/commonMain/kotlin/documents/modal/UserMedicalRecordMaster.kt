package documents.modal

data class UserMedicalRecordMaster(
    val id: String = "",
    val fileName: String = "",
    val fileUrl: String = "",
    val userId: String = "",
    val medicalRecordTypeId: String = "",
    val medicalRecordFor: String = "",
    val storagePath: String = "",
    val fileSize: String = "",
    val mimeType: String = "",
    val isActive: String = "", // 0 -> active, 1 -> inactive
    val createdAt: String = "",
    val updatedAt: String = ""
)
