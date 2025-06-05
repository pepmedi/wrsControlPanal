package hospital.repository

import core.data.HttpClientFactory
import core.data.safeCall
import core.domain.DataError
import core.domain.AppResult
import hospital.domain.HospitalMaster
import hospital.domain.HospitalRepository
import imageUpload.uploadImageToFirebaseStorage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import util.DatabaseCollection
import util.DatabaseDocumentsResponse
import util.DatabaseQueryResponse
import util.DatabaseRequest
import util.DatabaseResponse
import util.DatabaseUtil
import util.DatabaseValue
import util.StorageCollection
import util.buildCustomDatabaseQuery
import util.buildUpdateMask
import java.io.File

private const val BASE_URL = DatabaseUtil.DATABASE_URL

class HospitalRepositoryImpl(private val httpClient: HttpClient) : HospitalRepository {
    override suspend fun getAllHospital(): Flow<AppResult<List<HospitalMaster>, DataError.Remote>> =
        flow {
            val url = "$BASE_URL/hospitals"

            val result: AppResult<DatabaseDocumentsResponse, DataError.Remote> = safeCall {
                httpClient.get(url) {
                    contentType(ContentType.Application.Json)
                }
            }

            when (result) {
                is AppResult.Success -> {
                    val databaseResponse = result.data
                    val hospitals = databaseResponse.documents.map { databaseDocument ->
                        val fields = databaseDocument.fields
                        HospitalMaster(
                            id = databaseDocument.name.substringAfterLast("/"),
                            name = (fields["name"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            address = (fields["address"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            hospitalLogoUrl = (fields["hospitalLogoUrl"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            createdAt = (fields["createdAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            updatedAt = (fields["updatedAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty()
                        )
                    }
                    emit(AppResult.Success(hospitals))
                }

                is AppResult.Error -> {
                    emit(AppResult.Error(result.error))
                }
            }
        }.flowOn(Dispatchers.IO)

    override suspend fun addHospitalToDatabase(
        hospital: HospitalMaster,
        logoFile: File
    ): Flow<AppResult<HospitalMaster, DataError.Remote>> =
        flow {
            val url = "${BASE_URL}/hospitals"

            try {
                val response: HttpResponse = httpClient.post(url) {
                    contentType(ContentType.Application.Json)
                    setBody(
                        DatabaseRequest(
                            fields = mapOf(
                                "name" to DatabaseValue.StringValue(hospital.name),
                                "address" to DatabaseValue.StringValue(hospital.address),
                                "createdAt" to DatabaseValue.StringValue(hospital.createdAt),
                                "updatedAt" to DatabaseValue.StringValue(hospital.updatedAt)
                            )
                        )
                    )
                }

                if (response.status != HttpStatusCode.OK) {
                    println("Error: ${response.status}")
                    emit(AppResult.Error(DataError.Remote.SERVER))
                }

                val dataBaseResponse: DatabaseResponse = response.body()
                val generatedId = dataBaseResponse.name.substringAfterLast("/")

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
                    val hospitalImageUrl = uploadImageToFirebaseStorage(
                        httpClient = httpClient,
                        file = logoFile,
                        folderName = StorageCollection.HOSPITAL_LOGO,
                        fileName = generatedId
                    )
                    val patchImagesUrlResponse =
                        httpClient.patch("$BASE_URL/hospitals/$generatedId?updateMask.fieldPaths=hospitalLogoUrl") {
                            contentType(ContentType.Application.Json)
                            setBody(
                                DatabaseRequest(
                                    fields = mapOf(
                                        "hospitalLogoUrl" to DatabaseValue.StringValue(
                                            hospitalImageUrl
                                        )
                                    )
                                )
                            )
                        }

                    if (patchImagesUrlResponse.status == HttpStatusCode.OK) {
                        emit(
                            AppResult.Success(
                                hospital.copy(
                                    id = generatedId,
                                    hospitalLogoUrl = hospitalImageUrl
                                )
                            )
                        )
                    } else {
                        emit(AppResult.Error(DataError.Remote.SERVER))
                    }
                } else {
                    emit(AppResult.Error(DataError.Remote.SERVER))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println(e.localizedMessage)
                emit(AppResult.Error(DataError.Remote.SERVER))
            }
        }.flowOn(Dispatchers.IO)

    override suspend fun updateHospital(
        hospital: HospitalMaster,
        logoFile: File?
    ): Flow<AppResult<HospitalMaster, DataError.Remote>> =
        flow {
            // Step 1: Update doctor details first
            val patchResponse = httpClient.patch(
                "$BASE_URL/${DatabaseCollection.HOSPITALS}/${hospital.id}?${
                    buildUpdateMask(
                        "name",
                        "address",
                        "updatedAt"
                    )
                }"
            ) {
                contentType(ContentType.Application.Json)
                setBody(
                    DatabaseRequest(
                        fields = mapOf(
                            "name" to DatabaseValue.StringValue(hospital.name),
                            "address" to DatabaseValue.StringValue(hospital.address),
                            "updatedAt" to DatabaseValue.StringValue(hospital.updatedAt)
                        )
                    )
                )
            }

            if (patchResponse.status == HttpStatusCode.OK) {

                if (logoFile != null) {
                    val hospitalImageUrl = uploadImageToFirebaseStorage(
                        httpClient = httpClient,
                        file = logoFile,
                        folderName = StorageCollection.HOSPITAL_LOGO,
                        fileName = hospital.id
                    )
                    val patchImagesUrlResponse =
                        httpClient.patch("$BASE_URL/hospitals/${hospital.id}?updateMask.fieldPaths=hospitalLogoUrl") {
                            contentType(ContentType.Application.Json)
                            setBody(
                                DatabaseRequest(
                                    fields = mapOf(
                                        "hospitalLogoUrl" to DatabaseValue.StringValue(
                                            hospitalImageUrl
                                        )
                                    )
                                )
                            )
                        }

                    if (patchImagesUrlResponse.status == HttpStatusCode.OK) {
                        emit(AppResult.Success(hospital.copy(hospitalLogoUrl = hospitalImageUrl)))
                    } else {
                        emit(AppResult.Error(DataError.Remote.SERVER))
                    }

                } else {
                    emit(AppResult.Success(hospital))
                }
            } else {
                emit(AppResult.Error(DataError.Remote.SERVER))
            }

        }.flowOn(Dispatchers.IO)


    override suspend fun deleteHospital(hospital: HospitalMaster): Flow<AppResult<Unit, DataError.Remote>> =
        flow {
            println("📨 Sending query to find hospital with ID: ${hospital.id}")

            val requestBody = buildCustomDatabaseQuery(
                collection = DatabaseCollection.HOSPITALS,
                conditions = mapOf("id" to hospital.id)
            )

            val response: HttpResponse = httpClient.post(DatabaseUtil.DATABASE_QUERY_URL) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            if (response.status != HttpStatusCode.OK) {
                println("❌ Query failed: ${response.status}")
                emit(AppResult.Error(DataError.Remote.SERVER))
                return@flow
            }

            val databaseResponse: List<DatabaseQueryResponse> = try {
                HttpClientFactory.json.decodeFromString(response.bodyAsText())
            } catch (e: Exception) {
                listOf(HttpClientFactory.json.decodeFromString<DatabaseQueryResponse>(response.bodyAsText()))
            }

            if (databaseResponse.isEmpty()) {
                println("❌ No matching hospital found for deletion.")
                emit(AppResult.Error(DataError.Remote.SERVER))
                return@flow
            }

            val documentPath = databaseResponse.firstOrNull()?.document?.name
            if (documentPath.isNullOrEmpty()) {
                println("❌ No document path found in response.")
                emit(AppResult.Error(DataError.Remote.SERVER))
                return@flow
            }

            val documentId = documentPath.substringAfterLast("/")
            println("🗑️ Deleting hospital document with ID: $documentId")

            val deleteResponse =
                httpClient.delete("$BASE_URL/${DatabaseCollection.HOSPITALS}/$documentId")

            if (deleteResponse.status == HttpStatusCode.OK) {
                println("✅ Hospital deleted successfully.")
                emit(AppResult.Success(Unit))
            } else {
                println("❌ Failed to delete hospital. Status: ${deleteResponse.status}")
                emit(AppResult.Error(DataError.Remote.SERVER))
            }
        }.flowOn(Dispatchers.IO)
}