package appointment.domain

import core.domain.DataError
import core.domain.Result
import kotlinx.coroutines.flow.Flow

interface AppointmentBookingRepository {
    suspend fun getAllAppointments(): Flow<Result<List<AppointmentBookingMaster>, DataError.Remote>>
}