package documents.viewModal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import documents.modal.PatientDocumentRepository
import documents.modal.PatientMedicalRecordsMaster
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import java.io.File

class UploadAppointmentRecordsViewModal(private val patientDocumentRepository: PatientDocumentRepository) :
    ViewModel() {

    fun uploadAppointmentRecords(
        patientMedicalRecordsMaster: PatientMedicalRecordsMaster,
        document: File,
        mimeType: String
    ): Deferred<Result<Unit>> {
        return viewModelScope.async {
            patientDocumentRepository.addPatientDocument(
                patientMedicalRecordsMaster = patientMedicalRecordsMaster,
                document = document,
                mimeType = mimeType
            )
        }
    }
}