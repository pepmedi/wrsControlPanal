package controlPanalUser.repository

import controlPanalUser.domain.UserMasterControlPanel
import core.domain.DataError
import core.domain.Result
import controlPanalUser.domain.PanelUserRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import util.DatabaseCollection
import util.DatabaseRequest
import util.DatabaseResponse
import util.DatabaseUtil
import util.DatabaseValue

private const val BASE_URL = "${DatabaseUtil.DATABASE_URL}/${DatabaseCollection.PANEL_USER}"

class PanelUserRepositoryImpl(private val httpClient: HttpClient) : PanelUserRepository {
    override suspend fun createPanelUser(userMasterControlPanel: UserMasterControlPanel): Flow<Boolean> =
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
                                "doctorId" to DatabaseValue.StringValue(userMasterControlPanel.doctorId),
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
                    emit(false)
                    return@flow
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

                emit(patchResponse.status == HttpStatusCode.OK)
            } catch (e: Exception) {
                e.printStackTrace()
                println(e.localizedMessage)
                emit(false)
            }
        }.flowOn(Dispatchers.IO)

    override suspend fun updatePanelUser(userMasterControlPanel: UserMasterControlPanel): Flow<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun getPanelUser(userId: String): Result<UserMasterControlPanel, DataError.Remote> {
        TODO("Not yet implemented")
    }
}