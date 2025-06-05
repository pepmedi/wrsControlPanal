package documents.repository

import core.data.safeCall
import core.domain.AppResult
import core.domain.DataError
import documents.modal.MedicalRecordsRepository
import documents.modal.UserMedicalRecordMaster
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import util.DatabaseCollection
import util.DatabaseDocumentsResponse
import util.DatabaseUtil
import util.DatabaseValue

private const val BASE_URL =
    "${DatabaseUtil.DATABASE_URL}/${DatabaseCollection.USER_MEDICAL_RECORDS}"

class MedicalRecordsRepositoryImpl(private val httpClient: HttpClient) : MedicalRecordsRepository {
    override suspend fun getMedicalRecords(): Flow<AppResult<List<UserMedicalRecordMaster>, DataError.Remote>> {
        return flow {
            try {
                val result: AppResult<DatabaseDocumentsResponse, DataError.Remote> = safeCall {
                    httpClient.get(BASE_URL) {
                        contentType(ContentType.Application.Json)
                    }
                }

                when (result) {
                    is AppResult.Success -> {
                        val databaseResponse = result.data
                        val documents = databaseResponse.documents.map { databaseDocument ->
                            val fields = databaseDocument.fields
                            UserMedicalRecordMaster(
                                id = databaseDocument.name.substringAfterLast("/"),
                                userId = (fields["userId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                                fileName = (fields["fileName"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                                fileUrl = (fields["fileUrl"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                                storagePath = (fields["storagePath"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                                medicalRecordTypeId = (fields["medicalRecordTypeId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                                medicalRecordFor = (fields["medicalRecordFor"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                                mimeType = (fields["mimeType"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                                fileSize = (fields["fileSize"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                                isActive = (fields["isActive"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                                createdAt = (fields["createdAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                                updatedAt = (fields["updatedAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty()
                            )
                        }
                        emit(AppResult.Success(documents))
                    }

                    is AppResult.Error -> {
                        emit(AppResult.Error(DataError.Remote.SERVER))
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                println(e.message)
                emit(AppResult.Error(DataError.Remote.SERVER))
            }
        }
    }

}