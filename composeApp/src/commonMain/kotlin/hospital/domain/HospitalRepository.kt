package hospital.domain

import core.domain.DataError
import core.domain.AppResult
import kotlinx.coroutines.flow.Flow

interface HospitalRepository {
    suspend fun getAllHospital(): Flow<AppResult<List<HospitalMaster>, DataError.Remote>>
    suspend fun addHospitalToDatabase(hospital: HospitalMaster): Flow<AppResult<HospitalMaster, DataError.Remote>>
    suspend fun updateHospital(hospital: HospitalMaster): Flow<AppResult<HospitalMaster, DataError.Remote>>
    suspend fun deleteHospital(hospital: HospitalMaster): Flow<AppResult<Unit, DataError.Remote>>
}