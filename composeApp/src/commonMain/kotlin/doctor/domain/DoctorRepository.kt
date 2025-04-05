package doctor.domain

import core.domain.DataError
import core.domain.Result
import kotlinx.coroutines.flow.Flow
import java.io.File

interface DoctorRepository {
    suspend fun getAllDoctors(): Flow<Result<List<DoctorsMaster>, DataError.Remote>>
    suspend fun addDoctorToDatabase(doctor: DoctorsMaster, imageFile: File): Flow<Result<Unit, DataError.Remote>>
}