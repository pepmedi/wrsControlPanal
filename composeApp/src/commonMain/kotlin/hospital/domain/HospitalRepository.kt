package hospital.domain

import core.domain.DataError
import core.domain.Result
import kotlinx.coroutines.flow.Flow

interface HospitalRepository {
    suspend fun getAllHospital(): Flow<Result<List<HospitalMaster>, DataError.Remote>>
    suspend fun addHospitalToDatabase(hospital: HospitalMaster): Flow<Result<Unit, DataError.Remote>>
}