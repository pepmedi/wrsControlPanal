package doctor.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import core.domain.DataError
import core.domain.Result
import doctor.domain.DoctorMaster
import doctor.domain.DoctorRepository
import doctor.domain.DoctorSubmissionState
import hospital.domain.HospitalMaster
import hospital.domain.HospitalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import services.domain.ServicesMaster
import services.domain.ServicesRepository
import slots.domain.SlotsMaster
import slots.domain.SlotsRepository
import java.io.File

class DoctorViewModal(
    private val doctorRepository: DoctorRepository,
    private val hospitalRepository: HospitalRepository,
    private val servicesRepository: ServicesRepository,
    private val slotsRepository: SlotsRepository
) : ViewModel() {

    private val _doctorList =
        MutableStateFlow<Result<List<DoctorMaster>, DataError.Remote>>(Result.Success(emptyList()))
    val doctorList: StateFlow<Result<List<DoctorMaster>, DataError.Remote>> get() = _doctorList

    private val _hospitalList =
        MutableStateFlow<Result<List<HospitalMaster>, DataError.Remote>>(Result.Success(emptyList()))

    val hospitalList: StateFlow<Result<List<HospitalMaster>, DataError.Remote>> get() = _hospitalList

    private val _servicesList =
        MutableStateFlow<Result<List<ServicesMaster>, DataError.Remote>>(Result.Success(emptyList()))

    val servicesList: StateFlow<Result<List<ServicesMaster>, DataError.Remote>> get() = _servicesList

    private val _slotsList =
        MutableStateFlow<Result<List<SlotsMaster>, DataError>>(Result.Success(emptyList()))

    val slotsList: StateFlow<Result<List<SlotsMaster>, DataError>> get() = _slotsList

    private val _doctorSubmissionState =
        MutableStateFlow<DoctorSubmissionState>(DoctorSubmissionState.Idle)
    val doctorSubmissionState: StateFlow<DoctorSubmissionState> get() = _doctorSubmissionState

    init {
        getDoctorList()
        getAllHospital()
        getAllServices()
        getAllSlots()
    }

    private fun getAllSlots() {
        viewModelScope.launch {
            slotsRepository.getAllSlots()
                .collect { result ->
                    when (result) {
                        is Result.Success -> {
                            _slotsList.value = result
                        }

                        is Result.Error -> {

                        }
                    }
                }
        }
    }

    private fun getDoctorList() {
        println("fetched:-")
        viewModelScope.launch {
            doctorRepository.getAllDoctors()
                .collect { result ->
                    _doctorList.value = result
                }
        }
    }

    private fun getAllHospital() {
        viewModelScope.launch {
            hospitalRepository.getAllHospital()
                .collect { result ->
                    _hospitalList.value = result
                }
        }
    }

    private fun getAllServices() {
        viewModelScope.launch {
            servicesRepository.getAllServices()
                .collect {
                    _servicesList.value = it
                }
        }
    }

    fun addDoctor(doctorsMaster: DoctorMaster, file: File) {
        viewModelScope.launch {
            _doctorSubmissionState.value = DoctorSubmissionState.Loading // Show loading
            doctorRepository.addDoctorToDatabase(doctorsMaster, file)
                .collect { result ->
                    _doctorSubmissionState.value = when (result) {
                        is Result.Success -> DoctorSubmissionState.Success
                        is Result.Error -> DoctorSubmissionState.Error(result.error)
                    }
                }
        }
    }

    fun resetSubmissionState() {
        _doctorSubmissionState.value = DoctorSubmissionState.Idle
    }
}