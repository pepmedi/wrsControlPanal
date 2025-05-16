package services.domain

import core.domain.DataError
import core.domain.AppResult
import kotlinx.coroutines.flow.Flow
import java.io.File

interface ServicesRepository {
    suspend fun getAllServices(): Flow<AppResult<List<ServicesMaster>, DataError.Remote>>
    suspend fun addServiceToDatabase(
        service: ServicesMaster,
        imageFile: File,
        iconFile: File
    ): Flow<AppResult<ServicesMaster, DataError.Remote>>

    suspend fun updateService(
        service: ServicesMaster,
        iconFile: File?,
        imageFile: File?
    ): Flow<AppResult<ServicesMaster, DataError.Remote>>
}