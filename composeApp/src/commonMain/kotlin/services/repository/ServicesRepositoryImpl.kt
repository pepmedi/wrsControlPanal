package services.repository

import core.data.safeCall
import core.domain.DataError
import core.domain.AppResult
import imageUpload.uploadImageToFirebaseStorage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import services.domain.ServicesMaster
import services.domain.ServicesRepository
import util.DatabaseCollection
import util.DatabaseDocumentsResponse
import util.DatabaseRequest
import util.DatabaseResponse
import util.DatabaseUtil
import util.DatabaseValue
import util.StorageCollection
import util.buildUpdateMask
import java.io.File

private const val BASE_URL = "${DatabaseUtil.DATABASE_URL}/${DatabaseCollection.SERVICES}"

class ServicesRepositoryImpl(private val httpClient: HttpClient) : ServicesRepository {
    override suspend fun getAllServices(): Flow<AppResult<List<ServicesMaster>, DataError.Remote>> =
        flow {
            val result: AppResult<DatabaseDocumentsResponse, DataError.Remote> = safeCall {
                httpClient.get(BASE_URL) {
                    contentType(ContentType.Application.Json)
                }
            }

            when (result) {
                is AppResult.Success -> {
                    val databaseResponse = result.data
                    val services = databaseResponse.documents.map { databaseDocument ->
                        val fields = databaseDocument.fields
                        ServicesMaster(
                            id = databaseDocument.name.substringAfterLast("/"),
                            name = (fields["name"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            imageUrl = (fields["imageUrl"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            description = (fields["description"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            iconUrl = (fields["iconUrl"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            createdAt = (fields["createdAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            updatedAt = (fields["updatedAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty()
                        )
                    }
                    emit(AppResult.Success(services))
                }

                is AppResult.Error -> {
                    emit(AppResult.Error(DataError.Remote.SERVER))
                }
            }
        }.flowOn(Dispatchers.IO)

    override suspend fun addServiceToDatabase(
        service: ServicesMaster,
        imageFile: File,
        iconFile: File
    ): Flow<AppResult<ServicesMaster, DataError.Remote>> =
        flow {

            try {
                val response = httpClient.post(BASE_URL) {
                    contentType(ContentType.Application.Json)
                    setBody(
                        DatabaseRequest(
                            fields = mapOf(
                                "name" to DatabaseValue.StringValue(service.name),
                                "imageUrl" to DatabaseValue.StringValue(service.imageUrl),
                                "iconUrl" to DatabaseValue.StringValue(service.iconUrl),
                                "description" to DatabaseValue.StringValue(service.description),
                                "createdAt" to DatabaseValue.StringValue(service.createdAt),
                                "updatedAt" to DatabaseValue.StringValue(service.updatedAt)
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

                val patchResponse =
                    httpClient.patch("$BASE_URL/$generatedId?updateMask.fieldPaths=id") {
                        contentType(ContentType.Application.Json)
                        setBody(
                            DatabaseRequest(
                                fields = mapOf(
                                    "id" to DatabaseValue.StringValue(generatedId)
                                )
                            )
                        )
                    }

                if (patchResponse.status == HttpStatusCode.OK) {
                    val servicesImageUrl = uploadImageToFirebaseStorage(
                        httpClient = httpClient,
                        file = imageFile,
                        folderName = StorageCollection.SERVICE_IMAGES,
                        fileName = generatedId
                    )

                    val servicesIconUrl = uploadImageToFirebaseStorage(
                        httpClient = httpClient,
                        file = iconFile,
                        folderName = StorageCollection.SERVICE_ICON,
                        fileName = generatedId
                    )

                    val patchImagesUrlResponse =
                        httpClient.patch(
                            "$BASE_URL/$generatedId?${
                                buildUpdateMask(
                                    "imageUrl",
                                    "iconUrl"
                                )
                            }"
                        ) {
                            contentType(ContentType.Application.Json)
                            setBody(
                                DatabaseRequest(
                                    fields = mapOf(
                                        "imageUrl" to DatabaseValue.StringValue(servicesImageUrl),
                                        "iconUrl" to DatabaseValue.StringValue(servicesIconUrl)
                                    )
                                )
                            )
                        }

                    if (patchImagesUrlResponse.status == HttpStatusCode.OK) {
                        emit(
                            AppResult.Success(
                                service.copy(
                                    id = generatedId,
                                    imageUrl = servicesImageUrl,
                                    iconUrl = servicesIconUrl
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

    override suspend fun updateService(
        service: ServicesMaster,
        iconFile: File?,
        imageFile: File?
    ): Flow<AppResult<ServicesMaster, DataError.Remote>> =
        flow {
            try {
                val patchResponse = httpClient.patch(
                    "$BASE_URL/${service.id}?${
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
                                "name" to DatabaseValue.StringValue(service.name),
                                "description" to DatabaseValue.StringValue(service.description),
                                "updatedAt" to DatabaseValue.StringValue(service.updatedAt)
                            )
                        )
                    )
                }

                if (patchResponse.status == HttpStatusCode.OK) {

                    if (iconFile != null || imageFile != null) {
                        val updatedFields = mutableMapOf<String, DatabaseValue>()
                        var updatedImageUrl: String? = null
                        var updatedIconUrl: String? = null

                        // Upload profile image if available
                        if (imageFile != null) {
                            updatedImageUrl = uploadImageToFirebaseStorage(
                                httpClient = httpClient,
                                file = imageFile,
                                folderName = StorageCollection.SERVICE_IMAGES,
                                fileName = service.id
                            )
                            updatedFields["imageUrl"] = DatabaseValue.StringValue(updatedImageUrl)
                        }

                        // Upload icon if available
                        if (iconFile != null) {
                            updatedIconUrl = uploadImageToFirebaseStorage(
                                httpClient = httpClient,
                                file = iconFile,
                                folderName = StorageCollection.SERVICE_ICON,
                                fileName = service.id
                            )
                            updatedFields["iconUrl"] = DatabaseValue.StringValue(updatedIconUrl)
                        }

                        if (updatedFields.isNotEmpty()) {
                            val fieldPaths =
                                updatedFields.keys.joinToString("&updateMask.fieldPaths=")

                            val patchImageResponse = httpClient.patch(
                                "$BASE_URL/${DatabaseCollection.SERVICES}/${service.id}?updateMask.fieldPaths=$fieldPaths"
                            ) {
                                contentType(ContentType.Application.Json)
                                setBody(DatabaseRequest(fields = updatedFields))
                            }

                            if (patchImageResponse.status == HttpStatusCode.OK) {
                                emit(
                                    AppResult.Success(
                                        service.copy(
                                            imageUrl = updatedImageUrl ?: service.imageUrl,
                                            iconUrl = updatedIconUrl ?: service.iconUrl
                                        )
                                    )
                                ) // You can return a map or individual values
                            } else {
                                emit(AppResult.Error(DataError.Remote.SERVER))
                            }
                        } else {
                            // No files to upload or patch
                            emit(AppResult.Success(service))
                        }
                    } else {
                        emit(AppResult.Success(service))
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
}


