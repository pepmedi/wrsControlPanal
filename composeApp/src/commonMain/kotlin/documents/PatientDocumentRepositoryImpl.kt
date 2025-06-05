package documents

import core.data.safeCall
import core.domain.AppResult
import core.domain.DataError
import documents.modal.PatientDocumentRepository
import documents.modal.PatientMedicalRecordsMaster
import imageUpload.uploadDocumentToFirebaseStorage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import util.DatabaseCollection
import util.DatabaseDocumentsResponse
import util.DatabaseRequest
import util.DatabaseResponse
import util.DatabaseUtil
import util.DatabaseValue
import util.StorageCollection
import java.io.File

private const val BASE_URL = DatabaseUtil.DATABASE_URL

class PatientDocumentRepositoryImpl(private val httpClient: HttpClient) :
    PatientDocumentRepository {

    override suspend fun addPatientDocument(
        patientMedicalRecordsMaster: PatientMedicalRecordsMaster,
        document: File,
        mimeType: String
    ): Result<PatientMedicalRecordsMaster> {
        return try {
            val url = "$BASE_URL/${DatabaseCollection.PATIENT_MEDICAL_RECORDS}"

            val documentDetails = uploadDocumentToFirebaseStorage(
                httpClient = httpClient,
                byteArray = document.readBytes(),
                folderName = StorageCollection.PATIENT_MEDICAL_RECORD,
                id = patientMedicalRecordsMaster.appointmentId,
                mimeType = mimeType
            )

            val response: HttpResponse = httpClient.post(url) {
                contentType(ContentType.Application.Json)
                setBody(
                    DatabaseRequest(
                        fields = mapOf(
                            "appointmentId" to DatabaseValue.StringValue(patientMedicalRecordsMaster.appointmentId),
                            "recordName" to DatabaseValue.StringValue(patientMedicalRecordsMaster.recordName),
                            "description" to DatabaseValue.StringValue(patientMedicalRecordsMaster.description),
                            "fileUrl" to DatabaseValue.StringValue(documentDetails.downloadUrl),
                            "storagePath" to DatabaseValue.StringValue(documentDetails.storagePath),
                            "mimeType" to DatabaseValue.StringValue(mimeType),
                            "createdAt" to DatabaseValue.StringValue(patientMedicalRecordsMaster.createdAt),
                            "isActive" to DatabaseValue.StringValue(patientMedicalRecordsMaster.isActive)
                        )
                    )
                )
            }

            if (response.status.value == 200) {

                val databaseResponse: DatabaseResponse = response.body()
                val generatedId = databaseResponse.name.substringAfterLast("/")

                // Step 2: Update only the "id" field
                val patchResponse = httpClient.patch("$url/$generatedId?updateMask.fieldPaths=id") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        DatabaseRequest(
                            fields = mapOf(
                                "id" to DatabaseValue.StringValue(
                                    generatedId
                                )
                            )
                        )
                    )
                }

                if (patchResponse.status == HttpStatusCode.OK) {
                    Result.success(
                        patientMedicalRecordsMaster.copy(
                            id = generatedId,
                            fileUrl = documentDetails.downloadUrl,
                            storagePath = documentDetails.storagePath,
                            mimeType = mimeType
                        )
                    )
                } else {
                    Result.failure(Exception("Failed to update document ID"))
                }
            } else {
                Result.failure(Exception("Failed to upload document"))
            }

        } catch (e: Exception) {
            e.printStackTrace()
            println(e.localizedMessage)
            Result.failure(Exception(e.message))
        }
    }

    override suspend fun getAllPatientDocument(): AppResult<List<PatientMedicalRecordsMaster>, DataError.Remote> {
        val url = "$BASE_URL/${DatabaseCollection.PATIENT_MEDICAL_RECORDS}"
        return try {
            val result: AppResult<DatabaseDocumentsResponse, DataError.Remote> = safeCall {
                httpClient.get(url) {
                    contentType(ContentType.Application.Json)
                }
            }

            when (result) {
                is AppResult.Success -> {
                    val databaseResponse = result.data
                    val documents = databaseResponse.documents.map { document ->
                        val fields = document.fields
                        PatientMedicalRecordsMaster(
                            id = document.name.substringAfterLast("/"),
                            appointmentId = (fields["appointmentId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            recordName = (fields["recordName"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            description = (fields["description"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            fileUrl = (fields["fileUrl"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            storagePath = (fields["storagePath"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            mimeType = (fields["mimeType"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            createdAt = (fields["createdAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        )
                    }
                    AppResult.Success(documents)
                }

                is AppResult.Error -> {
                    AppResult.Error(result.error)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println(e.localizedMessage)
            AppResult.Error(DataError.Remote.SERVER)
        }
    }
}