package slots.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import core.domain.onError
import core.domain.onSuccess
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import slots.domain.SlotsMaster
import slots.domain.SlotsRepository

class AddSlotsViewModel(private val slotsRepository: SlotsRepository) : ViewModel() {

    fun addSlots(slotsMaster: SlotsMaster): Flow<Boolean> = callbackFlow {
        // Launch the repository call inside the coroutine
        viewModelScope.launch {
            slotsRepository.addSlotsToDatabase(slotsMaster)
                .onSuccess {
                    trySend(true) // Emit true on success
                }
                .onError {
                    trySend(false) // Emit false on error
                }
        }

        // Close the flow when the coroutine is done
        awaitClose { }
    }

}