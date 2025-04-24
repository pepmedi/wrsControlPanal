package slots.repository

import core.data.safeCall
import core.domain.DataError
import core.domain.AppResult
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
import slots.domain.SlotsMaster
import slots.domain.SlotsRepository
import util.DatabaseCollection
import util.DatabaseDocumentsResponse
import util.DatabaseRequest
import util.DatabaseResponse
import util.DatabaseUtil
import util.DatabaseValue

private const val BASE_URL = "${DatabaseUtil.DATABASE_URL}/${DatabaseCollection.SLOTS}"

class SlotsRepositoryImpl(private val httpClient: HttpClient) : SlotsRepository {
    override suspend fun addSlotsToDatabase(slotsMaster: SlotsMaster): AppResult<Boolean, DataError> {
        return try {
            val response = httpClient.post(BASE_URL) {
                contentType(ContentType.Application.Json)
                setBody(
                    DatabaseRequest(
                        fields = mapOf(
                            "name" to DatabaseValue.StringValue(slotsMaster.name),
                            "createdAt" to DatabaseValue.StringValue(slotsMaster.createdAt),
                            "updatedAt" to DatabaseValue.StringValue(slotsMaster.updatedAt)
                        )
                    )
                )
            }

            if (response.status != HttpStatusCode.OK) {
                println("Error: ${response.status}")
                AppResult.Error(DataError.Remote.SERVER)
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
                AppResult.Success(true)
            } else {
                AppResult.Error(DataError.Remote.SERVER)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            println(e.localizedMessage)
            AppResult.Error(DataError.Remote.SERVER)
        }
    }

    override suspend fun getAllSlots(): Flow<AppResult<List<SlotsMaster>, DataError>> =
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
                        SlotsMaster(
                            id = databaseDocument.name.substringAfterLast("/"),
                            name = (fields["name"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
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
}