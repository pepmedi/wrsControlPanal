package hospital.repository

import core.data.safeCall
import core.domain.DataError
import core.domain.Result
import hospital.domain.HospitalMaster
import hospital.domain.HospitalRepository
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
import util.DatabaseDocumentsResponse
import util.DatabaseRequest
import util.DatabaseResponse
import util.DatabaseUtil
import util.DatabaseValue

private const val BASE_URL = DatabaseUtil.DATABASE_URL

class DefaultHospitalRepository(private val httpClient: HttpClient) : HospitalRepository {
    override suspend fun getAllHospital(): Flow<Result<List<HospitalMaster>, DataError.Remote>> =
        flow {
            val url = "$BASE_URL/hospitals"

            val result: Result<DatabaseDocumentsResponse, DataError.Remote> = safeCall {
                httpClient.get(url) {
                    contentType(ContentType.Application.Json)
                }
            }

            when (result) {
                is Result.Success -> {
                    val databaseResponse = result.data
                    val hospitals = databaseResponse.documents.map { databaseDocument ->
                        val fields = databaseDocument.fields
                        HospitalMaster(
                            id = databaseDocument.name.substringAfterLast("/"),
                            name = (fields["name"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            address = (fields["address"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            createdAt = (fields["createdAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            updatedAt = (fields["updatedAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty()
                        )
                    }
                    emit(Result.Success(hospitals))
                }

                is Result.Error -> {
                    emit(Result.Error(result.error))
                }
            }
        }.flowOn(Dispatchers.IO)

    override suspend fun addHospitalToDatabase(hospital: HospitalMaster): Flow<Result<Unit, DataError.Remote>> =
        flow {
            val url = "${BASE_URL}/hospitals"

            try {
                val response: HttpResponse = httpClient.post(url) {
                    contentType(ContentType.Application.Json)
                    setBody(
                        DatabaseRequest(
                            fields = mapOf(
                                "name" to DatabaseValue.StringValue(hospital.name),
                                "address" to DatabaseValue.StringValue(hospital.address),
                                "createdAt" to DatabaseValue.StringValue(hospital.createdAt),
                                "updatedAt" to DatabaseValue.StringValue(hospital.updatedAt)
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

                val patchResponse = httpClient.patch("$url/$generatedId?updateMask.fieldPaths=id") {
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