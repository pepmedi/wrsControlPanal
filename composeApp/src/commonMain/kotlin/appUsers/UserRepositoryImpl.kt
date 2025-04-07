package appUsers

import core.data.safeCall
import core.domain.DataError
import core.domain.Result
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import util.DatabaseCollection
import util.DatabaseDocument
import util.DatabaseUtil
import util.DatabaseValue

private const val BASE_URL = "${DatabaseUtil.DATABASE_URL}/${DatabaseCollection.USERS}"

class UserRepositoryImpl(private val httpClient: HttpClient) : UserRepository {
    override suspend fun getUserDetails(userId: String): Flow<Result<User, DataError.Remote>> =
        flow {
            try {
                val result: Result<DatabaseDocument, DataError.Remote> = safeCall {
                    httpClient.get("${BASE_URL}/$userId") {
                        contentType(ContentType.Application.Json)
                    }
                }

                when (result) {
                    is Result.Success -> {
                        val documents = result.data
                        val fields = documents.fields
                        val user = User(
                            id = documents.name.substringAfterLast("/"),
                            name = (fields["name"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            mobileNo = (fields["mobileNo"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            email = (fields["email"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            dob = (fields["dob"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            gender = (fields["gender"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            password = (fields["password"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            createdAt = (fields["createdAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            updatedAt = (fields["updatedAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        )
                        emit(Result.Success(user))
                    }

                    is Result.Error -> {
                        emit(Result.Error(result.error))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println(e.localizedMessage)
                emit(Result.Error(DataError.Remote.SERVER))
            }
        }.flowOn(Dispatchers.IO)
}