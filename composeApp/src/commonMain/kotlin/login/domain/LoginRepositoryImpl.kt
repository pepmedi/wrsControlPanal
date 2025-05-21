package login.domain

import controlPanalUser.domain.UserMasterControlPanel
import core.data.HttpClientFactory.json
import core.data.safeCall
import core.domain.AppResult
import core.domain.DataError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import util.DatabaseCollection
import util.DatabaseDocument
import util.DatabaseQueryResponse
import util.DatabaseUtil
import util.DatabaseValue
import util.buildCustomDatabaseQuery

private const val BASE_URL = "${DatabaseUtil.DATABASE_URL}/${DatabaseCollection.PANEL_USER}"

class LoginRepositoryImpl(private val httpClient: HttpClient) : LoginRepository {

    override suspend fun isValidUser(
        username: String,
        password: String
    ): AppResult<UserMasterControlPanel, DataError.Remote> {
        try {
            val requestBody = buildCustomDatabaseQuery(
                collection = DatabaseCollection.PANEL_USER,
                conditions = mapOf(
                    "userName" to username,
                    "password" to password,
                    "isActive" to "0"
                )
            )
            val response: HttpResponse = httpClient.post(DatabaseUtil.DATABASE_QUERY_URL) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }
            val responseBody = response.body<String>()
            if (response.status.isSuccess()) {
                val isValidUser = responseBody.contains("document")
                val databaseResponses: List<DatabaseQueryResponse> =
                    json.decodeFromString(
                        ListSerializer(DatabaseQueryResponse.serializer()),
                        responseBody
                    )
                val documents = databaseResponses.mapNotNull { it.document }

                if (documents.isEmpty()) {
                    return AppResult.Error(DataError.Remote.SERVER)
                }

                val id = documents.first().name.substringAfterLast("/")
                if (isValidUser) {
                    val url = "${BASE_URL}/$id"
                    val result: AppResult<DatabaseDocument, DataError.Remote> = safeCall {
                        httpClient.get(url) {
                            contentType(ContentType.Application.Json)
                        }
                    }

                    when (result) {
                        is AppResult.Success -> {
                            val document = result.data

                            val fields = document.fields
                            val user = UserMasterControlPanel(
                                id = id,
                                userName = (fields["userName"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                                password = (fields["password"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                                empType = (fields["empType"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                                isActive = (fields["isActive"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                                doctorId = (fields["doctorId"] as? DatabaseValue.ArrayValue)?.values?.mapNotNull { (it as? DatabaseValue.StringValue)?.stringValue }
                                    .orEmpty(),
                                createdAt = (fields["createdAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                                updatedAt = (fields["updatedAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                                permissions = (fields["permissions"] as? DatabaseValue.ArrayValue)
                                    ?.values
                                    ?.mapNotNull { (it as? DatabaseValue.StringValue)?.stringValue }
                                    ?.toSet()
                                    .orEmpty(),
                            )
                            return AppResult.Success(user)
                        }

                        is AppResult.Error -> {
                            return AppResult.Error(result.error)
                        }
                    }
                }
            } else {
                return AppResult.Error(DataError.Remote.SERVER)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println(e.localizedMessage)
            return AppResult.Error(DataError.Remote.SERVER)
        }
        return AppResult.Error(DataError.Remote.SERVER)
    }
}

@Serializable
data class Value(
    val stringValue: String? = null,
    val arrayValue: ArrayValue? = null,
)

@Serializable
data class ArrayValue(
    val values: List<Value> = emptyList()
)