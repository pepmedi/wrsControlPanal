package appointment.domain

data class AppointmentBookingMaster(
    val id: String = "",
    val userId: String = "",
    val doctorId: String = "",
    val slotId: List<String> = emptyList(),
    val dates: List<String> = emptyList(),
    val status: String = "", // 0 -> completed , 1 -> Cancel , 2 -> Hold , 3-> upcoming
    val patientName: String = "",
    val age: String = "",
    val gender: String = "",
    val description: String = "",
    val hospitalsId: List<String> = emptyList(),
    val bookingFor: String = "", // 0-> yourSelf , 1 -> Other Person
    val mobileNo: String = "",
    val createdAt: String = "",
    val updatedAt: String = ""
)
