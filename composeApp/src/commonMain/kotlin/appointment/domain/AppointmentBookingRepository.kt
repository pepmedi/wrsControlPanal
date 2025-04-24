package appointment.domain

import core.domain.DataError
import core.domain.AppResult
import kotlinx.coroutines.flow.Flow

interface AppointmentBookingRepository {
    suspend fun getAllAppointments(): Flow<AppResult<List<AppointmentBookingMaster>, DataError.Remote>>
}