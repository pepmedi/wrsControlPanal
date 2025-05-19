package controlPanalUser.domain

import core.domain.DataError
import core.domain.AppResult
import kotlinx.coroutines.flow.Flow

interface PanelUserRepository {
    suspend fun createPanelUser(userMasterControlPanel: UserMasterControlPanel): Flow<AppResult<UserMasterControlPanel, DataError.Remote>>
    suspend fun updatePanelUser(userMasterControlPanel: UserMasterControlPanel): Flow<Boolean>
    suspend fun getPanelUser(userId: String): AppResult<UserMasterControlPanel, DataError.Remote>
    suspend fun getAllUser(): Flow<AppResult<List<UserMasterControlPanel>, DataError.Remote>>
    suspend fun deletePanelUser(userId: String): Flow<Boolean>
}