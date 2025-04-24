package imageUpload

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import util.DatabaseUtil.STORAGE_BUCKET
import util.Util.encodeFirebasePath
import util.Util.generateUUID
import util.Util.toEncodeURLPath
import util.getCurrentTimeStamp
import java.io.File
import java.util.UUID

suspend fun uploadImageToFirebaseStorage(
    httpClient: HttpClient,
    file: File,
    folderName: String,
    fileName: String = "${UUID.randomUUID()}.png",
): String {
    val storageBucket = STORAGE_BUCKET // Replace with your Firebase storage bucket
    val imagePath = "$folderName/$fileName"
    val url =
        "https://firebasestorage.googleapis.com/v0/b/$storageBucket/o?uploadType=media&name=$imagePath"


    val response: HttpResponse = httpClient.post(url) {
        contentType(ContentType.Image.PNG)
        setBody(file.readBytes())
    }

    if (response.status.isSuccess()) {
        val json = Json.decodeFromString<JsonObject>(response.bodyAsText())
        val downloadToken = json["downloadTokens"]?.jsonPrimitive?.content
            ?: throw Exception("Missing download token in response")
        val imagePathEncoded = imagePath.toEncodeURLPath()
        val downloadUrl =
            "https://firebasestorage.googleapis.com/v0/b/$storageBucket/o/$imagePathEncoded?alt=media&token=$downloadToken"

        println( "downloadUrl:- $downloadUrl")
        return downloadUrl
    } else {
        throw Exception("Image upload failed: ${response.status}")
    }
}

suspend fun uploadDocumentToFirebaseStorage(
    httpClient: HttpClient,
    byteArray: ByteArray,
    id: String,
    folderName: String,
    mimeType: String
): UploadedDocumentResult {
    val storageBucket = STORAGE_BUCKET
    val uploadedAt = getCurrentTimeStamp()

    val fileExtension = when (mimeType) {
        "application/pdf" -> "pdf"
        "image/jpeg", "image/jpg" -> "jpg"
        "image/png" -> "png"
        "image/webp" -> "webp"
        else -> "dat"
    }

    val uniqueFileName = "${generateUUID()}.$fileExtension"
    val storagePath = "$folderName/$id/$uniqueFileName"
    val encodedPath = storagePath.encodeFirebasePath()

    val url =
        "https://firebasestorage.googleapis.com/v0/b/$storageBucket/o?uploadType=media&name=$storagePath"
    println("downloadUrl:- $url")

    println("Uploading document:")
    println("→ FileName: $uniqueFileName")
    println("→ MimeType: $mimeType")
    println("→ StoragePath: $storagePath")
    println("→ Upload URL: $url")

    val contentType = ContentType.parse(mimeType)

    val response: HttpResponse = httpClient.post(url) {
        contentType(contentType)
        setBody(byteArray)
    }

    if (!response.status.isSuccess()) {
        println("Upload failed: ${response.status}")
        println("Response body: ${response.bodyAsText()}")
        throw Exception("Document upload failed: ${response.status}")
    }

    val responseText = response.bodyAsText()
    val json = Json.decodeFromString<JsonObject>(responseText)
    val downloadToken = json["downloadTokens"]?.jsonPrimitive?.content
        ?: throw Exception("Missing download token in response")

    val downloadUrl =
        "https://firebasestorage.googleapis.com/v0/b/$storageBucket/o/$encodedPath?alt=media&token=$downloadToken"

    return UploadedDocumentResult(
        downloadUrl = downloadUrl,
        storagePath = storagePath,
        mimeType = mimeType,
        uploadedAt = uploadedAt
    )
}

data class UploadedDocumentResult(
    val downloadUrl: String,
    val storagePath: String,
    val mimeType: String,
    val uploadedAt: String
)
