package blog.repository

import blog.domain.BlogMaster
import blog.domain.BlogRepository
import core.domain.DataError
import core.domain.AppResult
import imageUpload.uploadImageToFirebaseStorage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import util.DatabaseCollection
import util.DatabaseRequest
import util.DatabaseResponse
import util.DatabaseUtil
import util.DatabaseValue
import util.StorageCollection
import java.io.File

private const val BASE_URL = "${DatabaseUtil.DATABASE_URL}/${DatabaseCollection.BLOGS}"

class BlogRepositoryImpl(private val httpClient: HttpClient) : BlogRepository {
    override suspend fun addBlogToDatabase(
        blog: BlogMaster,
        imageFile: File
    ): AppResult<Unit, DataError.Remote> {

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
                    AppResult.Success(Unit)
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
}