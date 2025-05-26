package documents.viewModal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import core.domain.onError
import core.domain.onSuccess
import documents.modal.MedicalRecordsRepository
import documents.modal.PatientDocumentRepository
import documents.modal.PatientMedicalRecordsMaster
import documents.modal.UserMedicalRecordMaster
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AllRecordsViewModel(
    private val patientDocumentRepository: PatientDocumentRepository,
    private val userMedicalRecordRepository: MedicalRecordsRepository
) :
    ViewModel() {

    private val _state = MutableStateFlow(AllRecordsUiState())
    val state = _state.asStateFlow()
        .onStart {
            getAllRecords()
            getUserHealthRecord()
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

    fun addMedicalRecord(patientMedicalRecordsMaster: PatientMedicalRecordsMaster) {
        _state.update { it.copy(records = it.records + patientMedicalRecordsMaster) }
    }

    private fun getUserHealthRecord() {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            userMedicalRecordRepository
                .getMedicalRecords()
                .collect { result ->
                    result.onSuccess { records ->
                        _state.update { it.copy(userHealthRecord = records, isLoading = false) }
                    }
                }
        }
    }
}

data class AllRecordsUiState(
    val isLoading: Boolean = false,
    val records: List<PatientMedicalRecordsMaster> = emptyList(),
    val userHealthRecord: List<UserMedicalRecordMaster> = emptyList(),
    val error: String? = null
)