package blog.repository

import blog.domain.BlogMaster
import blog.domain.BlogRepository
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
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import util.DatabaseCollection
import util.DatabaseDocumentsResponse
import util.DatabaseRequest
import util.DatabaseResponse
import util.DatabaseUtil
import util.DatabaseValue
import util.StorageCollection
import util.buildUpdateMask
import java.io.File

private const val BASE_URL = "${DatabaseUtil.DATABASE_URL}/${DatabaseCollection.BLOGS}"

class BlogRepositoryImpl(private val httpClient: HttpClient) : BlogRepository {
    override suspend fun addBlogToDatabase(
        blog: BlogMaster,
        imageFile: File
    ): AppResult<BlogMaster, DataError.Remote> {

        return try {
            val response: HttpResponse = httpClient.post(BASE_URL) {
                contentType(ContentType.Application.Json)
                setBody(
                    DatabaseRequest(
                        fields = mapOf(
                            "title" to DatabaseValue.StringValue(blog.title),
                            "description" to DatabaseValue.StringValue(blog.description),
                            "createdAt" to DatabaseValue.StringValue(blog.createdAt),
                            "updatedAt" to DatabaseValue.StringValue(blog.updatedAt),
                            "doctorId" to DatabaseValue.StringValue(blog.doctorId),
                        )
                    )
                )
            }
            if (response.status != HttpStatusCode.OK) {
                println("Error: ${response.status}")
                AppResult.Error(DataError.Remote.SERVER)
            }

            val firestoreResponse: DatabaseResponse = response.body()
            val generatedId = firestoreResponse.name.substringAfterLast("/")

            // Step 2: Update only the "id" field
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
                val downloadUrl = uploadImageToFirebaseStorage(
                    httpClient = httpClient,
                    file = imageFile,
                    folderName = StorageCollection.BLOG_IMAGES,
                    fileName = generatedId
                )

                val patchImageResponse =
                    httpClient.patch("$BASE_URL/$generatedId?updateMask.fieldPaths=imageUrl") {
                        contentType(ContentType.Application.Json)
                        setBody(
                            DatabaseRequest(
                                fields = mapOf(
                                    "imageUrl" to DatabaseValue.StringValue(
                                        downloadUrl
                                    )
                                )
                            )
                        )
                    }

                if (patchImageResponse.status == HttpStatusCode.OK) {
                    AppResult.Success(blog.copy(id = generatedId, imageUrl = downloadUrl))
                } else {
                    AppResult.Error(DataError.Remote.SERVER)
                }
            } else {
                AppResult.Error(DataError.Remote.SERVER)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println(e.message)
            AppResult.Error(DataError.Remote.SERVER)
        }
    }

    override suspend fun getAllBlogs(): Flow<AppResult<List<BlogMaster>, DataError.Remote>> = flow {
        try {
            val result: AppResult<DatabaseDocumentsResponse, DataError.Remote> = safeCall {
                httpClient.get(BASE_URL) {
                    contentType(ContentType.Application.Json)
                }
            }

            when (result) {
                is AppResult.Success -> {
                    val firestoreResponse = result.data
                    val blogs = firestoreResponse.documents.map { document ->
                        val fields = document.fields
                        BlogMaster(
                            id = document.name.substringAfterLast("/"),
                            title = (fields["title"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            description = (fields["description"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            imageUrl = (fields["imageUrl"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            updatedAt = (fields["updatedAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            doctorId = (fields["doctorId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            createdAt = (fields["createdAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty()
                        )
                    }
                    emit(AppResult.Success(blogs))
                }

                is AppResult.Error -> {
                    emit(AppResult.Error(result.error))
                }
            }

        } catch (e: Exception) {
            println(e.message)
            emit(AppResult.Error(DataError.Remote.SERVER))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun updateBlog(
        blog: BlogMaster,
        imageFile: File?
    ): Flow<AppResult<BlogMaster, DataError.Remote>> = flow {

        try {
            val patchResponse =
                httpClient.patch(
                    "$BASE_URL/${blog.id}?${
                        buildUpdateMask(
                            "title",
                            "description",
                            "updatedAt",
                            "doctorId"
                        )
                    }"
                ) {
                    contentType(ContentType.Application.Json)
                    setBody(
                        DatabaseRequest(
                            fields = mapOf(
                                "title" to DatabaseValue.StringValue(blog.title),
                                "description" to DatabaseValue.StringValue(blog.description),
                                "updatedAt" to DatabaseValue.StringValue(blog.updatedAt),
                                "doctorId" to DatabaseValue.StringValue(blog.doctorId)
                            )
                        )
                    )
                }

            if (patchResponse.status == HttpStatusCode.OK) {
                if (imageFile != null) {
                    val downloadUrl = uploadImageToFirebaseStorage(
                        httpClient = httpClient,
                        file = imageFile,
                        folderName = StorageCollection.BLOG_IMAGES,
                        fileName = blog.id
                    )

                    val patchImageResponse = httpClient.patch(
                        "$BASE_URL/${blog.id}?updateMask.fieldPaths=imageUrl"
                    ) {
                        contentType(ContentType.Application.Json)
                        setBody(
                            DatabaseRequest(
                                fields = mapOf(
                                    "imageUrl" to DatabaseValue.StringValue(
                                        downloadUrl
                                    )
                                )
                            )
                        )
                    }

                    if (patchImageResponse.status == HttpStatusCode.OK) {
                        emit(AppResult.Success(blog.copy(imageUrl = downloadUrl)))
                    } else {
                        emit(AppResult.Error(DataError.Remote.SERVER))
                    }
                } else {
                    emit(AppResult.Success(blog))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println(e.localizedMessage)
            emit(AppResult.Error(DataError.Remote.SERVER))
        }
    }
}