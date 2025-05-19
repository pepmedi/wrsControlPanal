package di


import appUsers.UserRepository
import appUsers.UserRepositoryImpl
import appointment.domain.AppointmentBookingRepository
import appointment.presentation.AppointmentDetailsViewModel
import appointment.repository.AppointmentBookingRepositoryImpl
import appointment.presentation.AppointmentsViewModel
import blog.domain.BlogRepository
import blog.repository.BlogRepositoryImpl
import blog.viewModel.AddBlogViewModel
import blog.viewModel.AllBLogListViewModel
import blog.viewModel.UpdateBlogViewModel
import controlPanalUser.domain.PanelUserRepository
import controlPanalUser.viewModel.PanelUserCreationViewModel
import controlPanalUser.viewModel.PanelUserScreenViewModel
import controlPanalUser.repository.PanelUserRepositoryImpl
import controlPanalUser.viewModel.UpdatePanelUserViewModel
import core.data.HttpClientFactory
import doctor.domain.DoctorRepository
import doctor.viewModal.AddDoctorViewModel
import doctor.repository.DoctorRepositoryImpl
import doctor.viewModal.DoctorListViewModel
import doctor.viewModal.UpdateDoctorViewModel
import documents.PatientDocumentRepositoryImpl
import documents.modal.PatientDocumentRepository
import documents.viewModal.AllRecordsViewModal
import documents.viewModal.UploadAppointmentRecordsViewModal
import hospital.domain.HospitalRepository
import hospital.presentation.HospitalViewModel
import hospital.repository.HospitalRepositoryImpl
import login.domain.LoginRepository
import login.domain.LoginRepositoryImpl
import login.presentation.LoginViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module
import services.domain.ServicesRepository
import services.viewModel.ServicesViewModel
import services.repository.ServicesRepositoryImpl
import services.viewModel.AllServicesListViewModel
import services.viewModel.UpdateServicesViewModel
import slots.domain.SlotsRepository
import slots.repository.SlotsRepositoryImpl
import slots.viewModel.AddSlotsViewModel

expect val platformModule: Module

val sharedModule = module {
    single { HttpClientFactory.create(get()) }
    singleOf(::DoctorRepositoryImpl).bind<DoctorRepository>()
    singleOf(::HospitalRepositoryImpl).bind<HospitalRepository>()
    singleOf(::ServicesRepositoryImpl).bind<ServicesRepository>()
    singleOf(::PanelUserRepositoryImpl).bind<PanelUserRepository>()
    singleOf(::LoginRepositoryImpl).bind<LoginRepository>()
    singleOf(::AppointmentBookingRepositoryImpl).bind<AppointmentBookingRepository>()
    singleOf(::AppointmentBookingRepositoryImpl).bind<AppointmentBookingRepository>()
    singleOf(::UserRepositoryImpl).bind<UserRepository>()
    singleOf(::SlotsRepositoryImpl).bind<SlotsRepository>()
    singleOf(::BlogRepositoryImpl).bind<BlogRepository>()
    singleOf(::PatientDocumentRepositoryImpl).bind<PatientDocumentRepository>()

    viewModel { AddDoctorViewModel(get(), get(), get(), get()) }
    viewModel { HospitalViewModel(get()) }
    viewModel { ServicesViewModel(get()) }
    viewModel { PanelUserCreationViewModel(get(), get()) }
    viewModel { LoginViewModel(get()) }
    viewModel { AppointmentsViewModel(get(), get()) }
    viewModel { PanelUserScreenViewModel(get()) }
    viewModel { AppointmentDetailsViewModel(get()) }
    viewModel { AddSlotsViewModel(get()) }
    viewModel { AddBlogViewModel(get(), get()) }
    viewModel { DoctorListViewModel(get()) }
    viewModel { UpdateDoctorViewModel(get(), get(), get(), get()) }
    viewModel { UploadAppointmentRecordsViewModal(get()) }
    viewModel { AllRecordsViewModal(get()) }
    viewModel { AllBLogListViewModel(get()) }
    viewModel { UpdateBlogViewModel(get(), get()) }
    viewModel { AllServicesListViewModel(get()) }
    viewModel { UpdateServicesViewModel(get()) }

    viewModel { UpdatePanelUserViewModel(get(), get()) }
}