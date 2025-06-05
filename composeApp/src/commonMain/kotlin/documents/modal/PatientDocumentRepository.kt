package documents.modal

import core.domain.AppResult
import core.domain.DataError
import java.io.File

interface PatientDocumentRepository {
    suspend fun addPatientDocument(
        patientMedicalRecordsMaster: PatientMedicalRecordsMaster,
        document: File,
        mimeType: String
    ): Result<PatientMedicalRecordsMaster>

    suspend fun getAllPatientDocument(): AppResult<List<PatientMedicalRecordsMaster>, DataError.Remote>
}