package doctor.domain

import core.domain.DataError
import core.domain.AppResult
import kotlinx.coroutines.flow.Flow
import java.io.File

interface DoctorRepository {
    suspend fun getAllDoctors(): Flow<AppResult<List<DoctorMaster>, DataError.Remote>>
    suspend fun addDoctorToDatabase(
        doctor: DoctorMaster,
        profileImageFile: File,
        infoImageFile: File,
        doctorHomePageImage: File
    ): Flow<AppResult<DoctorMaster, DataError.Remote>>

    suspend fun getDoctor(doctorId: String): Flow<AppResult<DoctorMaster, DataError.Remote>>

    suspend fun updateDoctor(
        doctor: DoctorMaster,
        profileImageFile: File?,
        infoImageFile: File?,
        doctorHomePageImage: File?
    ): Flow<AppResult<Triple<String?, String?, String?>, DataError.Remote>>

    suspend fun blockDoctorDates(
        doctorId: String,
        blockedDates: List<String>
    ): Flow<AppResult<Unit, DataError.Remote>>
}