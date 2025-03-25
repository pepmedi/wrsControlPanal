package di


import core.data.HttpClientFactory
import doctor.domain.DoctorRepository
import doctor.presentation.DoctorViewModal
import doctor.repository.DefaultDoctorRepository
import hospital.domain.HospitalRepository
import hospital.presentation.HospitalViewModal
import hospital.repository.DefaultHospitalRepository
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module
import services.domain.ServicesRepository
import services.presentation.ServicesViewModal
import services.repository.DefaultServicesRepository

expect val platformModule: Module

val sharedModule = module {
    single { HttpClientFactory.create(get()) }
    singleOf(::DefaultDoctorRepository).bind<DoctorRepository>()
    singleOf(::DefaultHospitalRepository).bind<HospitalRepository>()
    singleOf(::DefaultServicesRepository).bind<ServicesRepository>()

    viewModel { DoctorViewModal(get(),get(),get()) }
    viewModel{HospitalViewModal(get())}
    viewModel { ServicesViewModal(get()) }
}