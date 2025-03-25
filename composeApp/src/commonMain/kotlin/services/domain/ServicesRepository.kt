package services.domain

import core.domain.DataError
import core.domain.Result
import kotlinx.coroutines.flow.Flow

interface ServicesRepository {
    suspend fun getAllServices(): Flow<Result<List<ServicesMaster>, DataError.Remote>>
    suspend fun addServiceToDatabase(service: ServicesMaster): Flow<Result<Unit, DataError.Remote>>
}