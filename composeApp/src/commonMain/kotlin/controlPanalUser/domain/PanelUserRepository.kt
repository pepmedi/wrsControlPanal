package controlPanalUser.domain

import core.domain.DataError
import core.domain.Result
import kotlinx.coroutines.flow.Flow

interface PanelUserRepository {
    suspend fun createPanelUser(userMasterControlPanel: UserMasterControlPanel): Flow<Boolean>
    suspend fun updatePanelUser(userMasterControlPanel: UserMasterControlPanel): Flow<Boolean>
    suspend fun getPanelUser(userId: String): Result<UserMasterControlPanel, DataError.Remote>
}