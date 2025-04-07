package appointment.repository

import appointment.domain.AppointmentBookingMaster
import appointment.domain.AppointmentBookingRepository
import core.data.safeCall
import core.domain.DataError
import core.domain.Result
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import util.DatabaseCollection
import util.DatabaseDocumentsResponse
import util.DatabaseUtil
import util.DatabaseValue

private const val BASE_URL = "${DatabaseUtil.DATABASE_URL}/${DatabaseCollection.APPOINTMENTS}"

class AppointmentBookingRepositoryImpl(private val httpClient: HttpClient) :
    AppointmentBookingRepository {
    override suspend fun getAllAppointments(): Flow<Result<List<AppointmentBookingMaster>, DataError.Remote>> =
        flow {
            val result: Result<DatabaseDocumentsResponse, DataError.Remote> = safeCall {
                httpClient.get(BASE_URL) {
                    contentType(ContentType.Application.Json)
                }
            }

            when (result) {
                is Result.Success -> {
                    val databaseResponse = result.data
                    val appointments = databaseResponse.documents.map { appointments ->
                        val field = appointments.fields
                        AppointmentBookingMaster(
                            id = appointments.name.substringAfterLast("/"),
                            userId = (field["userId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            doctorId = (field["doctorId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            status = (field["status"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            patientName = (field["patientName"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            age = (field["age"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            gender = (field["gender"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            description = (field["description"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            createdAt = (field["createdAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            mobileNo = (field["mobileNo"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            updatedAt = (field["updatedAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            slotId = (field["slotId"] as? DatabaseValue.ArrayValue)?.values?.mapNotNull { (it as? DatabaseValue.StringValue)?.stringValue }
                                .orEmpty(),
                            hospitalsId = (field["hospitalsId"] as? DatabaseValue.ArrayValue)?.values?.mapNotNull { (it as? DatabaseValue.StringValue)?.stringValue }
                                .orEmpty(),
                            dates = (field["dates"] as? DatabaseValue.ArrayValue)?.values?.mapNotNull { (it as? DatabaseValue.StringValue)?.stringValue }
                                .orEmpty(),
                        )
                    }
                    emit(Result.Success(appointments))
                }

                is Result.Error -> {
                    emit(Result.Error(DataError.Remote.SERVER))
                }
            }
        }.flowOn(Dispatchers.IO)


}