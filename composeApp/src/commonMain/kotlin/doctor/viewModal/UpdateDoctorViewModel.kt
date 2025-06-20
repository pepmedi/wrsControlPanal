package doctor.viewModal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import core.domain.AppResult
import doctor.domain.DoctorMaster
import doctor.domain.DoctorRepository
import hospital.domain.HospitalMaster
import hospital.domain.HospitalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import services.domain.ServicesMaster
import services.domain.ServicesRepository
import slots.domain.SlotsMaster
import slots.domain.SlotsRepository
import util.getCurrentTimeStamp
import java.io.File

class UpdateDoctorViewModel(
    private val doctorRepository: DoctorRepository,
    private val hospitalRepository: HospitalRepository,
    private val servicesRepository: ServicesRepository,
    private val slotsRepository: SlotsRepository
) : ViewModel() {
    private val _state = MutableStateFlow(UpdateDoctorUiState())
    val state: StateFlow<UpdateDoctorUiState> = _state.asStateFlow()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            UpdateDoctorUiState()
        )

    fun onAction(action: UpdateDoctorActions) {
        when (action) {
            is UpdateDoctorActions.OnUpdateClick -> {
                if (_state.value.isFormValid) {
                    updateDoctor()
                }
            }

            is UpdateDoctorActions.OnDoctorNameChange -> {
                _state.update { it.copy(doctorDetails = it.doctorDetails.copy(name = action.name)) }
            }

            is UpdateDoctorActions.OnAgeChange -> {
                _state.update { it.copy(doctorDetails = it.doctorDetails.copy(age = action.age)) }
            }

            is UpdateDoctorActions.OnExperienceChange -> {
                _state.update { it.copy(doctorDetails = it.doctorDetails.copy(experience = action.experience)) }
            }

            is UpdateDoctorActions.OnQualificationChange -> {
                _state.update { it.copy(doctorDetails = it.doctorDetails.copy(qualification = action.qualification)) }
            }

            is UpdateDoctorActions.OnHospitalChange -> {
                _state.update { it.copy(selectedHospitals = action.hospitals) }
            }

            is UpdateDoctorActions.OnServicesChange -> {
                _state.update { it.copy(selectedServices = action.services) }
            }

            is UpdateDoctorActions.OnSlotsChange -> {
                _state.update { it.copy(selectedSlots = action.slots) }
            }

            is UpdateDoctorActions.OnProfilePicChange -> {
                _state.update { it.copy(profilePicChange = action.pic) }
            }

            is UpdateDoctorActions.OnSpecialityChange -> {
                _state.update { it.copy(doctorDetails = it.doctorDetails.copy(speciality = action.speciality)) }
            }

            is UpdateDoctorActions.OnFocusChange -> {
                _state.update { it.copy(doctorDetails = it.doctorDetails.copy(focus = action.focus)) }
            }

            is UpdateDoctorActions.OnProfileTextChange -> {
                _state.update { it.copy(doctorDetails = it.doctorDetails.copy(profile = action.text)) }
            }

            is UpdateDoctorActions.OnCareerPathChange -> {
                _state.update { it.copy(doctorDetails = it.doctorDetails.copy(careerPath = action.path)) }
            }

            is UpdateDoctorActions.OnDoctorInfoPicChange -> {
                _state.update { it.copy(infoPicChange = action.pic) }
            }

            is UpdateDoctorActions.OnDoctorHomePagePicChange -> {
                _state.update { it.copy(doctorHomePageImage = action.pic) }
            }

            is UpdateDoctorActions.OnCityChange -> {
                _state.update { it.copy(doctorDetails = it.doctorDetails.copy(city = action.city)) }
            }
        }
    }

    private fun updateDoctor() {
        val doctorState = _state.value.doctorDetails
        _state.update { it.copy(isUpdating = true) }
        viewModelScope.launch {
            doctorRepository
                .updateDoctor(
                    doctor = DoctorMaster(
                        id = doctorState.id,
                        name = doctorState.name,
                        age = doctorState.age,
                        experience = doctorState.experience,
                        consltFee = doctorState.consltFee,
                        speciality = doctorState.speciality,
                        focus = doctorState.focus,
                        profile = doctorState.profile,
                        careerPath = doctorState.careerPath,
                        city = doctorState.city,
                        hospital = _state.value.selectedHospitals.map { it.id },
                        services = _state.value.selectedServices.map { it.id },
                        slots = _state.value.selectedSlots.map { it.id },
                        qualification = doctorState.qualification,
                        updatedAt = getCurrentTimeStamp()
                    ),
                    profileImageFile = _state.value.profilePicChange,
                    infoImageFile = _state.value.infoPicChange,
                    doctorHomePageImage = _state.value.doctorHomePageImage
                )
                .collect { result ->
                    when (result) {
                        is AppResult.Success -> {
                            val (profilePic, doctorInfoPic) = result.data

                            _state.update {
                                it.copy(
                                    doctorDetails = doctorState.copy(
                                        profilePic = profilePic ?: doctorState.profilePic,
                                        doctorInfoPic = doctorInfoPic
                                            ?: doctorState.doctorInfoPic
                                    ),
                                    isUpdating = false,
                                    isSuccessful = true
                                )
                            }
                            _state.update { it.copy(isSuccessful = true) }
                        }

                        is AppResult.Error -> {
                            _state.update { it.copy(error = result.error.toString()) }
                        }
                    }
                }
        }
    }

    fun resetData() {
        _state.value = UpdateDoctorUiState()
    }

    fun getDoctor(doctorId: String) {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            doctorRepository.getDoctor(doctorId = doctorId)
                .collect { result ->
                    when (result) {
                        is AppResult.Success -> {
                            getAllHospital()
                            getAllServices()
                            getAllSlots()

                            val selectedHospitals = _state.value.hospitalList.filter {
                                it.id in result.data.hospital
                            }
                            val selectedServices = _state.value.servicesList.filter {
                                it.id in result.data.services
                            }
                            val selectedSlots = _state.value.slotsList.filter {
                                it.id in result.data.slots
                            }

                            _state.update {
                                it.copy(
                                    doctorDetails = result.data,
                                    selectedHospitals = selectedHospitals,
                                    selectedServices = selectedServices,
                                    selectedSlots = selectedSlots,
                                    isLoading = false
                                )
                            }
                        }

                        is AppResult.Error -> {
                            _state.update { it.copy(error = result.error.toString()) }
                        }
                    }
                }
        }
    }

    private suspend fun getAllHospital() {
        hospitalRepository.getAllHospital()
            .collect { result ->
                when (result) {
                    is AppResult.Success -> {
                        _state.update { it.copy(hospitalList = result.data) }
                    }

                    is AppResult.Error -> {
                        _state.update { it.copy(error = result.error.toString()) }
                    }
                }
            }
    }

    private suspend fun getAllServices() {

        servicesRepository.getAllServices()
            .collect { result ->
                when (result) {
                    is AppResult.Success -> {
                        _state.update { it.copy(servicesList = result.data) }
                    }

                    is AppResult.Error -> {
                        _state.update { it.copy(error = result.error.toString()) }
                    }
                }
            }

    }

    private suspend fun getAllSlots() {
        slotsRepository
            .getAllSlots()
            .collect { result ->
                when (result) {
                    is AppResult.Success -> {
                        _state.update { it.copy(slotsList = result.data) }
                    }

                    is AppResult.Error -> {
                        _state.update { it.copy(error = result.error.toString()) }
                    }
                }
            }
    }
}

data class UpdateDoctorUiState(
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val isSuccessful: Boolean = false,
    val error: String? = null,
    val doctorDetails: DoctorMaster = DoctorMaster(),
    val hospitalList: List<HospitalMaster> = emptyList(),
    val selectedHospitals: List<HospitalMaster> = emptyList(),

    val servicesList: List<ServicesMaster> = emptyList(),
    val selectedServices: List<ServicesMaster> = emptyList(),

    val slotsList: List<SlotsMaster> = emptyList(),
    val selectedSlots: List<SlotsMaster> = emptyList(),
    val profilePicChange: File? = null,
    val infoPicChange: File? = null,
    val doctorHomePageImage: File? = null
) {
    val isFormValid: Boolean
        get() = selectedHospitals.isNotEmpty() && selectedServices.isNotEmpty()
                && selectedSlots.isNotEmpty() && doctorDetails.name.isNotEmpty()
                && doctorDetails.age.isNotEmpty() && doctorDetails.qualification.isNotEmpty()
                && doctorDetails.experience.isNotEmpty() && doctorDetails.profilePic.isNotEmpty()
                && (doctorDetails.doctorInfoPic.isNotEmpty() || infoPicChange != null)

    fun getErrorMessage(): String {
        return when {
            selectedHospitals.isEmpty() -> "Please select at least one hospital."
            selectedServices.isEmpty() -> "Please select at least one service."
            selectedSlots.isEmpty() -> "Please select at least one slot."
            doctorDetails.name.isEmpty() -> "Doctor name cannot be empty."
            doctorDetails.age.isEmpty() -> "Doctor age cannot be empty."
            doctorDetails.qualification.isEmpty() -> "Qualification fee cannot be empty."
            doctorDetails.experience.isEmpty() -> "Doctor experience cannot be empty."
            doctorDetails.profilePic.isEmpty() -> "Profile picture cannot be empty."
            doctorDetails.city.isEmpty() -> "City cannot be empty."
            (doctorDetails.doctorInfoPic.isEmpty() || infoPicChange != null) -> "Doctor info picture cannot be empty."
            else -> ""
        }
    }
}

sealed interface UpdateDoctorActions {
    data object OnUpdateClick : UpdateDoctorActions
    data class OnDoctorNameChange(val name: String) : UpdateDoctorActions
    data class OnAgeChange(val age: String) : UpdateDoctorActions
    data class OnExperienceChange(val experience: String) : UpdateDoctorActions
    data class OnCityChange(val city: String) : UpdateDoctorActions
    data class OnQualificationChange(val qualification: String) : UpdateDoctorActions
    data class OnSpecialityChange(val speciality: String) : UpdateDoctorActions
    data class OnHospitalChange(val hospitals: List<HospitalMaster>) : UpdateDoctorActions
    data class OnServicesChange(val services: List<ServicesMaster>) : UpdateDoctorActions
    data class OnSlotsChange(val slots: List<SlotsMaster>) : UpdateDoctorActions
    data class OnProfilePicChange(val pic: File?) : UpdateDoctorActions
    data class OnDoctorInfoPicChange(val pic: File?) : UpdateDoctorActions
    data class OnDoctorHomePagePicChange(val pic: File?) : UpdateDoctorActions
    data class OnCareerPathChange(val path: String) : UpdateDoctorActions
    data class OnFocusChange(val focus: String) : UpdateDoctorActions
    data class OnProfileTextChange(val text: String) : UpdateDoctorActions
}