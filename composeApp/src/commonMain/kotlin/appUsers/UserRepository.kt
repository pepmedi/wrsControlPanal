package appUsers

import core.domain.DataError
import core.domain.Result
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getUserDetails(userId: String): Flow<Result<User, DataError.Remote>>
}