package doctor.domain

import core.domain.DataError
import core.domain.Result
import kotlinx.coroutines.flow.Flow
import java.io.File

interface DoctorRepository {
    suspend fun getAllDoctors(): Flow<Result<List<DoctorMaster>, DataError.Remote>>
    suspend fun addDoctorToDatabase(doctor: DoctorMaster, imageFile: File): Flow<Result<Unit, DataError.Remote>>
    suspend fun getDoctor(doctorId:String):Flow<Result<DoctorMaster, DataError.Remote>>
    suspend fun updateDoctor(doctor: DoctorMaster, imageFile: File?):Flow<Result<String?, DataError.Remote>>
}