package doctor.viewModal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import core.domain.DataError
import core.domain.AppResult
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

class AddDoctorViewModel(
    private val doctorRepository: DoctorRepository,
    private val hospitalRepository: HospitalRepository,
    private val servicesRepository: ServicesRepository,
    private val slotsRepository: SlotsRepository
) : ViewModel() {

    private val _doctorList =
        MutableStateFlow<AppResult<List<DoctorMaster>, DataError.Remote>>(
            AppResult.Success(
                emptyList()
            )
        )
    val doctorList: StateFlow<AppResult<List<DoctorMaster>, DataError.Remote>> get() = _doctorList

    private val _hospitalList =
        MutableStateFlow<AppResult<List<HospitalMaster>, DataError.Remote>>(
            AppResult.Success(
                emptyList()
            )
        )

    val hospitalList: StateFlow<AppResult<List<HospitalMaster>, DataError.Remote>> get() = _hospitalList

    private val _servicesList =
        MutableStateFlow<AppResult<List<ServicesMaster>, DataError.Remote>>(
            AppResult.Success(
                emptyList()
            )
        )

    val servicesList: StateFlow<AppResult<List<ServicesMaster>, DataError.Remote>> get() = _servicesList

    private val _slotsList =
        MutableStateFlow<AppResult<List<SlotsMaster>, DataError>>(AppResult.Success(emptyList()))

    val slotsList: StateFlow<AppResult<List<SlotsMaster>, DataError>> get() = _slotsList

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
                        is AppResult.Success -> {
                            _slotsList.value = result
                        }

                        is AppResult.Error -> {

                        }
                    }
                }
        }
    }

    fun reset() {
        _doctorSubmissionState.value = DoctorSubmissionState.Idle
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

    fun addDoctor(doctorsMaster: DoctorMaster, doctorProfileImage: File, doctorInfoImage: File) {
        viewModelScope.launch {
            _doctorSubmissionState.value = DoctorSubmissionState.Loading // Show loading
            doctorRepository.addDoctorToDatabase(
                doctor = doctorsMaster,
                profileImageFile = doctorProfileImage,
                infoImageFile = doctorInfoImage
            )
                .collect { result ->
                    _doctorSubmissionState.value = when (result) {
                        is AppResult.Success -> DoctorSubmissionState.Success(result.data)
                        is AppResult.Error -> DoctorSubmissionState.Error(result.error)
                    }
                }
        }
    }

    fun resetSubmissionState() {
        _doctorSubmissionState.value = DoctorSubmissionState.Idle
    }
}