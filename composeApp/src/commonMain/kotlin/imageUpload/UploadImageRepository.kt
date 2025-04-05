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
import util.Util.toEncodeURLPath
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
