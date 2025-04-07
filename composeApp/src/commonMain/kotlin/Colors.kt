import androidx.compose.ui.graphics.Color

val DarkBlue = Color(0xFF0B405E)
val DesertWhite = Color(0xFFF7F7F7)
val SandYellow = Color(0xFFFFBD64)
val LightBlue = Color(0xFF9AD9FF)
//val PrimaryAppColor = hexToComposeColor("#7ddced")
val PrimaryAppColor = hexToComposeColor("#8dd8f8")
val SecondaryAppColor = hexToComposeColor("#38B6FF")


fun hexToComposeColor(hex: String): Color {
    val colorInt = hex.removePrefix("#").toLong(16) or 0x00000000FF000000
    return Color(colorInt)
}

val BackgroundColors = listOf(
    Color(0xFFE3F2FD), // Light Blue
    Color(0xFFFFEBEE), // Light Red
    Color(0xFFE8F5E9), // Light Green
    Color(0xFFFFF3E0), // Light Orange
    Color(0xFFF3E5F5)  // Light Purple
)