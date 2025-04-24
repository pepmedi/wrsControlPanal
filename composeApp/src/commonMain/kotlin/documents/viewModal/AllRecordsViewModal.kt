package documents.viewModal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import core.domain.onError
import core.domain.onSuccess
import documents.modal.PatientDocumentRepository
import documents.modal.PatientMedicalRecordsMaster
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class AllRecordsViewModal(private val patientDocumentRepository: PatientDocumentRepository) :
    ViewModel() {

    private val _state = MutableStateFlow(AllRecordsUiState())
    val state = _state.asStateFlow()
        .onStart {
            getAllRecords()
        }
        .stateIn(
            viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            initialValue = _state.value
        )

    private suspend fun getAllRecords() {
        _state.update { it.copy(isLoading = true) }
        patientDocumentRepository
            .getAllPatientDocument()
            .onSuccess { records ->
                _state.update { it.copy(records = records, isLoading = false) }
            }
            .onError { error ->
                _state.update { it.copy(isLoading = false, error = error.name) }
            }
    }
}

data class AllRecordsUiState(
    val isLoading: Boolean = false,
    val records: List<PatientMedicalRecordsMaster> = emptyList(),
    val error: String? = null
)