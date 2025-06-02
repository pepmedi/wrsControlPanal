package achievements.repository

import achievements.data.AchievementMaster
import achievements.data.AchievementsRepository
import app.App
import core.data.HttpClientFactory
import core.data.safeCall
import core.domain.AppResult
import core.domain.DataError
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

private const val BASE_URL =
    "${DatabaseUtil.DATABASE_URL}/${DatabaseCollection.ACHIEVEMENT_COLLECTION}"

class AchievementsRepositoryImpl(private val httpClient: HttpClient) : AchievementsRepository {
    override suspend fun getAllAchievements(): Flow<AppResult<List<AchievementMaster>, DataError.Remote>> =
        flow {

            val result: AppResult<DatabaseDocumentsResponse, DataError.Remote> = safeCall {
                httpClient.get(BASE_URL) {
                    contentType(ContentType.Application.Json)
                }
            }

            when (result) {
                is AppResult.Success -> {
                    val databaseResponse = result.data
                    val achievements = databaseResponse.documents.map { databaseDocument ->
                        val fields = databaseDocument.fields
                        AchievementMaster(
                            id = databaseDocument.name.substringAfterLast("/"),
                            name = (fields["name"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            description = (fields["description"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            imageUrl = (fields["imageUrl"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            createdAt = (fields["createdAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            updatedAt = (fields["updatedAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        )
                    }
                    emit(AppResult.Success(achievements))
                }

                is AppResult.Error -> {
                    emit(AppResult.Error(DataError.Remote.SERVER))
                }

            }
        }.flowOn(Dispatchers.IO)

    override suspend fun addAchievement(
        achievementMaster: AchievementMaster,
        image: File
    ): Flow<AppResult<AchievementMaster, DataError.Remote>> = flow {

        try {

            val response: HttpResponse = httpClient.post(BASE_URL) {
                contentType(ContentType.Application.Json)
                setBody(
                    DatabaseRequest(
                        fields = mapOf(
                            "name" to DatabaseValue.StringValue(achievementMaster.name),
                            "description" to DatabaseValue.StringValue(achievementMaster.description),
                            "imageUrl" to DatabaseValue.StringValue(achievementMaster.imageUrl),
                            "createdAt" to DatabaseValue.StringValue(achievementMaster.createdAt),
                            "updatedAt" to DatabaseValue.StringValue(achievementMaster.updatedAt)
                        )
                    )
                )
            }

            if (response.status.value != 200) {
                emit(AppResult.Error(DataError.Remote.SERVER))
            }

            val dataBaseResponse: DatabaseResponse = response.body()
            val generatedId = dataBaseResponse.name.substringAfterLast("/")

            val patchResponse =
                httpClient.patch("$BASE_URL/$generatedId?updateMask.fieldPaths=id") {
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
                val achievementImageUrl = uploadImageToFirebaseStorage(
                    httpClient = httpClient,
                    file = image,
                    folderName = StorageCollection.ACHIEVEMENT_IMAGES,
                    fileName = generatedId
                )

                val patchImagesUrlResponse =
                    httpClient.patch("${BASE_URL}/$generatedId?updateMask.fieldPaths=imageUrl") {
                        contentType(ContentType.Application.Json)
                        setBody(
                            DatabaseRequest(
                                fields = mapOf(
                                    "imageUrl" to DatabaseValue.StringValue(
                                        achievementImageUrl
                                    )
                                )
                            )
                        )
                    }

                if (patchImagesUrlResponse.status == HttpStatusCode.OK) {
                    emit(
                        AppResult.Success(
                            achievementMaster.copy(
                                id = generatedId,
                                imageUrl = achievementImageUrl
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
            println(e.localizedMessage)
            e.printStackTrace()
            emit(AppResult.Error(DataError.Remote.SERVER))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun updateAchievement(
        achievementMaster: AchievementMaster,
        image: File?
    ): Flow<AppResult<AchievementMaster, DataError.Remote>> = flow {
        // Step 1: Update doctor details first
        val patchResponse = httpClient.patch(
            "${BASE_URL}/${achievementMaster.id}?${
                buildUpdateMask(
                    "name",
                    "description",
                    "updatedAt"
                )
            }"
        ) {
            contentType(ContentType.Application.Json)
            setBody(
                DatabaseRequest(
                    fields = mapOf(
                        "name" to DatabaseValue.StringValue(achievementMaster.name),
                        "description" to DatabaseValue.StringValue(achievementMaster.description),
                        "updatedAt" to DatabaseValue.StringValue(achievementMaster.updatedAt)
                    )
                )
            )
        }

        if (patchResponse.status == HttpStatusCode.OK) {
            if (image != null) {
                val achievementImageUrl = uploadImageToFirebaseStorage(
                    httpClient = httpClient,
                    file = image,
                    folderName = StorageCollection.ACHIEVEMENT_IMAGES,
                    fileName = achievementMaster.id
                )

                val patchImagesUrlResponse =
                    httpClient.patch("${BASE_URL}/${achievementMaster.id}?updateMask.fieldPaths=imageUrl") {
                        contentType(ContentType.Application.Json)
                        setBody(
                            DatabaseRequest(
                                fields = mapOf(
                                    "imageUrl" to DatabaseValue.StringValue(
                                        achievementImageUrl
                                    )
                                )
                            )
                        )
                    }

                if (patchImagesUrlResponse.status == HttpStatusCode.OK) {
                    emit(
                        AppResult.Success(achievementMaster.copy(imageUrl = achievementImageUrl))
                    )
                } else {
                    emit(AppResult.Error(DataError.Remote.SERVER))
                }

            } else {
                emit(AppResult.Success(achievementMaster))
            }
        }
    }.flowOn(Dispatchers.IO)


    override suspend fun deleteAchievement(achievementMaster: AchievementMaster): Flow<AppResult<Unit, DataError.Remote>> =
        flow {

            val requestBody = buildCustomDatabaseQuery(
                collection = DatabaseCollection.ACHIEVEMENT_COLLECTION,
                conditions = mapOf("id" to achievementMaster.id)
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
                httpClient.delete("${BASE_URL}/$documentId")

            if (deleteResponse.status == HttpStatusCode.OK) {
                println("✅ Hospital deleted successfully.")
                emit(AppResult.Success(Unit))
            } else {
                println("❌ Failed to delete hospital. Status: ${deleteResponse.status}")
                emit(AppResult.Error(DataError.Remote.SERVER))
            }
        }.flowOn(Dispatchers.IO)
}
