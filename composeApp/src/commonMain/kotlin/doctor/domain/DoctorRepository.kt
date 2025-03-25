package doctor.domain

import core.domain.DataError
import kotlinx.coroutines.flow.Flow
import  core.domain.Result

interface DoctorRepository {
    fun getAllDoctors(): Flow<Result<List<DoctorsMaster>, DataError.Remote>>
    suspend fun addDoctorToDatabase(doctor: DoctorsMaster): Flow<Result<Unit, DataError.Remote>>
}