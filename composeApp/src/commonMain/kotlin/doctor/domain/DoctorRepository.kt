package doctor.domain

import core.domain.DataError
import core.domain.AppResult
import kotlinx.coroutines.flow.Flow
import java.io.File

interface DoctorRepository {
    suspend fun getAllDoctors(): Flow<AppResult<List<DoctorMaster>, DataError.Remote>>
    suspend fun addDoctorToDatabase(doctor: DoctorMaster, imageFile: File): Flow<AppResult<Unit, DataError.Remote>>
    suspend fun getDoctor(doctorId:String):Flow<AppResult<DoctorMaster, DataError.Remote>>
    suspend fun updateDoctor(doctor: DoctorMaster, imageFile: File?):Flow<AppResult<String?, DataError.Remote>>
}