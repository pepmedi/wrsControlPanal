package controlPanalUser.repository

import controlPanalUser.domain.UserMasterControlPanel
import core.domain.DataError
import core.domain.AppResult
import controlPanalUser.domain.PanelUserRepository
import core.data.HttpClientFactory
import core.data.safeCall
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
import io.ktor.http.isSuccess
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
import util.buildCustomDatabaseQuery
import util.buildUpdateMask

private const val BASE_URL = "${DatabaseUtil.DATABASE_URL}/${DatabaseCollection.PANEL_USER}"

class PanelUserRepositoryImpl(private val httpClient: HttpClient) : PanelUserRepository {
    override suspend fun createPanelUser(userMasterControlPanel: UserMasterControlPanel): Flow<AppResult<UserMasterControlPanel, DataError.Remote>> =
        flow {
            try {
                val response = httpClient.post(BASE_URL) {
                    contentType(ContentType.Application.Json)
                    setBody(
                        DatabaseRequest(
                            fields = mapOf(
                                "userName" to DatabaseValue.StringValue(userMasterControlPanel.userName),
                                "password" to DatabaseValue.StringValue(userMasterControlPanel.password),
                                "empType" to DatabaseValue.StringValue(userMasterControlPanel.empType),
                                "isActive" to DatabaseValue.StringValue(userMasterControlPanel.isActive),
                                "doctorId" to DatabaseValue.ArrayValue(userMasterControlPanel.doctorId.map {
                                    DatabaseValue.StringValue(it)
                                }),
                                "createdAt" to DatabaseValue.StringValue(userMasterControlPanel.createdAt),
                                "updatedAt" to DatabaseValue.StringValue(userMasterControlPanel.updatedAt),
                                "permissions" to DatabaseValue.ArrayValue(
                                    userMasterControlPanel.permissions.map {
                                        DatabaseValue.StringValue(
                                            it
                                        )
                                    }
                                ),
                            )
                        )
                    )
                }

                if (response.status != HttpStatusCode.OK) {
                    println("Error: ${response.status}  ${response.bodyAsText()}")
                    emit(AppResult.Error(DataError.Remote.SERVER))
                }

                val databaseResponse: DatabaseResponse = response.body()
                val generatedId = databaseResponse.name.substringAfterLast("/")

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
                    emit(AppResult.Success(userMasterControlPanel.copy(id = generatedId)))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println(e.localizedMessage)
                emit(AppResult.Error(DataError.Remote.SERVER))
            }
        }.flowOn(Dispatchers.IO)

    override suspend fun updatePanelUser(userMasterControlPanel: UserMasterControlPanel): Flow<Boolean> =
        flow {
            try {
                val patchResponse = httpClient.patch(
                    "${BASE_URL}/${userMasterControlPanel.id}?${
                        buildUpdateMask(
                            "isActive",
                            "updatedAt"
                        )
                    }"
                ) {
                    contentType(ContentType.Application.Json)
                    setBody(
                        DatabaseRequest(
                            fields = mapOf(
                                "isActive" to DatabaseValue.StringValue(if (userMasterControlPanel.isActive == "0") "1" else "0"),
                                "updatedAt" to DatabaseValue.StringValue(
                                    System.currentTimeMillis().toString()
                                )
                            )
                        )
                    )
                }

                if (patchResponse.status.isSuccess()) {
                    emit(true)
                } else {
                    emit(false)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                println(e.localizedMessage)
                emit(false)
            }
        }.flowOn(Dispatchers.IO)

    override suspend fun getPanelUser(userId: String): AppResult<UserMasterControlPanel, DataError.Remote> {
        TODO("Not yet implemented")
    }

    override suspend fun getAllUser(): Flow<AppResult<List<UserMasterControlPanel>, DataError.Remote>> =
        flow {
            val result: AppResult<DatabaseDocumentsResponse, DataError.Remote> = safeCall {
                httpClient.get(BASE_URL) {
                    contentType(ContentType.Application.Json)
                }
            }

            when (result) {
                is AppResult.Success -> {
                    val databaseResponse = result.data
                    val users = databaseResponse.documents.map { user ->
                        val field = user.fields
                        UserMasterControlPanel(
                            id = user.name.substringAfterLast("/"),
                            userName = (field["userName"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            password = (field["password"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            empType = (field["empType"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            isActive = (field["isActive"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            doctorId = (field["doctorId"] as? DatabaseValue.ArrayValue)?.values?.mapNotNull { (it as? DatabaseValue.StringValue)?.stringValue }
                                .orEmpty(),
                            createdAt = (field["createdAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            updatedAt = (field["updatedAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            permissions = (field["permissions"] as? DatabaseValue.ArrayValue)
                                ?.values
                                ?.mapNotNull { (it as? DatabaseValue.StringValue)?.stringValue }
                                ?.toSet()
                                .orEmpty(),
                        )
                    }
                    emit(AppResult.Success(users))
                }

                is AppResult.Error -> {
                    emit(AppResult.Error(DataError.Remote.SERVER))
                }
            }
        }

    override suspend fun deletePanelUser(userId: String): Flow<Boolean> = flow {

        val requestBody = buildCustomDatabaseQuery(
            collection = DatabaseCollection.PANEL_USER,
            conditions = mapOf("id" to userId)
        )

        val response: HttpResponse = httpClient.post(DatabaseUtil.DATABASE_QUERY_URL) {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        if (response.status != HttpStatusCode.OK) {
            println("❌ Query failed: ${response.status}")
            emit(false)
            return@flow
        }

        val databaseResponse: List<DatabaseQueryResponse> = try {
            HttpClientFactory.json.decodeFromString(response.bodyAsText())
        } catch (e: Exception) {
            listOf(HttpClientFactory.json.decodeFromString<DatabaseQueryResponse>(response.bodyAsText()))
        }

        if (databaseResponse.isEmpty()) {
            println("❌ No matching Panel user found for deletion.")
            emit(false)
            return@flow
        }

        val documentPath = databaseResponse.firstOrNull()?.document?.name
        if (documentPath.isNullOrEmpty()) {
            println("❌ No document path found in response.")
            emit(false)
            return@flow
        }

        val documentId = documentPath.substringAfterLast("/")
        println("🗑️ Deleting Panel user document with ID: $documentId")

        val deleteResponse =
            httpClient.delete("${BASE_URL}/$documentId")

        if (deleteResponse.status == HttpStatusCode.OK) {
            println("✅ Panel User deleted successfully.")
            emit(true)
        } else {
            println("❌ Failed to delete Panel user. Status: ${deleteResponse.status}")
            emit(false)
        }
    }
}