package appUsers

import core.domain.DataError
import core.domain.AppResult
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getUserDetails(userId: String): Flow<AppResult<User, DataError.Remote>>
}