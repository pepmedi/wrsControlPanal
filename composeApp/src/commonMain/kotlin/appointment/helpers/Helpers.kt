package appointment.helpers


enum class AppointmentTab(val label: String, val type: AppointmentStatus) {
    ALL("All", AppointmentStatus.ALL),
    WAITING("Waiting", AppointmentStatus.WAITING),
    UPCOMING("Upcoming", AppointmentStatus.UPCOMING),
    COMPLETED("Completed", AppointmentStatus.COMPLETED),
    CANCELLED("Cancelled", AppointmentStatus.CANCELLED)
}

enum class AppointmentStatus {
    ALL,
    WAITING,
    UPCOMING,
    COMPLETED,
    CANCELLED
}