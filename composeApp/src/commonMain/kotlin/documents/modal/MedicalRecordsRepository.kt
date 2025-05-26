package documents.modal

import core.domain.AppResult
import core.domain.DataError
import kotlinx.coroutines.flow.Flow

interface MedicalRecordsRepository {
    suspend fun getMedicalRecords(): Flow<AppResult<List<UserMedicalRecordMaster>, DataError.Remote>>

}