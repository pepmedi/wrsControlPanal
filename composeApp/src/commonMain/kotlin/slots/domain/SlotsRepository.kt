package slots.domain

import core.domain.DataError
import core.domain.Result
import kotlinx.coroutines.flow.Flow

interface SlotsRepository {
    suspend fun addSlotsToDatabase(slotsMaster: SlotsMaster): Result<Boolean, DataError>
    suspend fun getAllSlots(): Flow<Result<List<SlotsMaster>, DataError>>
}