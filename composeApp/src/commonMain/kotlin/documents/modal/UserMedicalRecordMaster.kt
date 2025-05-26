package documents.modal

data class UserMedicalRecordMaster(
    val id: String = "",
    val fileName: String = "",
    val fileUrl: String = "",
    val userId: String = "",
    val medicalRecordTypeId: String = "",
    val medicalRecordFor: String = "", // like prescription, report, etc.
    val storagePath: String = "",
    val mimeType: String = "",
    val isActive: String = "", // 0 -> active, 1 -> inactive
    val createdAt: String = "",
    val updatedAt: String = ""
)
