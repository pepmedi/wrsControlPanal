package slots.domain

import core.domain.DataError
import core.domain.AppResult
import kotlinx.coroutines.flow.Flow

interface SlotsRepository {
    suspend fun addSlotsToDatabase(slotsMaster: SlotsMaster): AppResult<Boolean, DataError>
    suspend fun getAllSlots(): Flow<AppResult<List<SlotsMaster>, DataError>>
}