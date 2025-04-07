package di


import appUsers.UserRepository
import appUsers.UserRepositoryImpl
import appointment.domain.AppointmentBookingRepository
import appointment.presentation.AppointmentDetailsViewModal
import appointment.repository.AppointmentBookingRepositoryImpl
import appointment.presentation.AppointmentsViewModal
import controlPanalUser.domain.PanelUserRepository
import controlPanalUser.presentation.PanelUserCreationViewModal
import controlPanalUser.presentation.PanelUserScreenViewModal
import controlPanalUser.repository.PanelUserRepositoryImpl
import core.data.HttpClientFactory
import doctor.domain.DoctorRepository
import doctor.presentation.DoctorViewModal
import doctor.repository.DoctorRepositoryImpl
import hospital.domain.HospitalRepository
import hospital.presentation.HospitalViewModal
import hospital.repository.DefaultHospitalRepository
import login.domain.LoginRepository
import login.domain.LoginRepositoryImpl
import login.presentation.LoginViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module
import services.domain.ServicesRepository
import services.presentation.ServicesViewModal
import services.repository.ServicesRepositoryImpl

expect val platformModule: Module

val sharedModule = module {
    single { HttpClientFactory.create(get()) }
    singleOf(::DoctorRepositoryImpl).bind<DoctorRepository>()
    singleOf(::DefaultHospitalRepository).bind<HospitalRepository>()
    singleOf(::ServicesRepositoryImpl).bind<ServicesRepository>()
    singleOf(::PanelUserRepositoryImpl).bind<PanelUserRepository>()
    singleOf(::LoginRepositoryImpl).bind<LoginRepository>()
    singleOf(::AppointmentBookingRepositoryImpl).bind<AppointmentBookingRepository>()
    singleOf(::AppointmentBookingRepositoryImpl).bind<AppointmentBookingRepository>()
    singleOf(::UserRepositoryImpl).bind<UserRepository>()

    viewModel { DoctorViewModal(get(), get(), get()) }
    viewModel { HospitalViewModal(get()) }
    viewModel { ServicesViewModal(get()) }
    viewModel { PanelUserCreationViewModal(get(), get()) }
    viewModel { LoginViewModel(get()) }
    viewModel { AppointmentsViewModal(get(), get(), get()) }
    viewModel { PanelUserScreenViewModal(get()) }
    viewModel { AppointmentDetailsViewModal(get()) }
}