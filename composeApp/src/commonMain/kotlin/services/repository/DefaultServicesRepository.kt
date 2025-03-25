package services.repository

import core.data.safeCall
import core.domain.DataError
import core.domain.Result
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
import util.DatabaseDocumentsResponse
import util.DatabaseRequest
import util.DatabaseResponse
import util.DatabaseUtil
import util.DatabaseValue
import wrscontrolpanel.composeapp.generated.resources.Res

private const val BASE_URL = "${DatabaseUtil.DATABASE_URL}/services"

class DefaultServicesRepository(private val httpClient: HttpClient) : ServicesRepository {
    override suspend fun getAllServices(): Flow<Result<List<ServicesMaster>, DataError.Remote>> =
        flow {
            val result: Result<DatabaseDocumentsResponse, DataError.Remote> = safeCall {
                httpClient.get(BASE_URL) {
                    contentType(ContentType.Application.Json)
                }
            }

            when (result) {
                is Result.Success -> {
                    val databaseResponse = result.data
                    val services = databaseResponse.documents.map { databaseDocument ->
                        val fields = databaseDocument.fields
                        ServicesMaster(
                            id = databaseDocument.name.substringAfterLast("/"),
                            name = (fields["name"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            createdAt = (fields["createdAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            updatedAt = (fields["updatedAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty()
                        )
                    }
                    emit(Result.Success(services))
                }

                is Result.Error -> {
                    emit(Result.Error(DataError.Remote.SERVER))
                }
            }
        }.flowOn(Dispatchers.IO)

    override suspend fun addServiceToDatabase(service: ServicesMaster): Flow<Result<Unit, DataError.Remote>> =
        flow {

            try {
                val response = httpClient.post(BASE_URL) {
                    contentType(ContentType.Application.Json)
                    setBody(
                        DatabaseRequest(
                            fields = mapOf(
                                "name" to DatabaseValue.StringValue(service.name),
                                "createdAt" to DatabaseValue.StringValue(service.createdAt),
                                "updatedAt" to DatabaseValue.StringValue(service.updatedAt)
                            )
                        )
                    )
                }

                if (response.status != HttpStatusCode.OK) {
                    println("Error: ${response.status}")
                    emit(Result.Error(DataError.Remote.SERVER))
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
                    emit(Result.Success(Unit))
                } else {
                    emit(Result.Error(DataError.Remote.SERVER))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println(e.localizedMessage)
                emit(Result.Error(DataError.Remote.SERVER))
            }

        }.flowOn(Dispatchers.IO)
}