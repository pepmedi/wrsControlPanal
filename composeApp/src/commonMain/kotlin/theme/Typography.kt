package theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import wrscontrolpanel.composeapp.generated.resources.Manrope_Bold
import wrscontrolpanel.composeapp.generated.resources.Manrope_ExtraBold
import wrscontrolpanel.composeapp.generated.resources.Manrope_ExtraLight
import wrscontrolpanel.composeapp.generated.resources.Manrope_Light
import wrscontrolpanel.composeapp.generated.resources.Manrope_Medium
import wrscontrolpanel.composeapp.generated.resources.Manrope_Regular
import wrscontrolpanel.composeapp.generated.resources.Manrope_SemiBold
import wrscontrolpanel.composeapp.generated.resources.Res

@Composable
fun manropeFontFamily() = FontFamily(
    Font(Res.font.Manrope_ExtraLight, FontWeight.ExtraLight),
    Font(Res.font.Manrope_Light, FontWeight.Light),
    Font(Res.font.Manrope_Regular, FontWeight.Normal),
    Font(Res.font.Manrope_Medium, FontWeight.Medium),
    Font(Res.font.Manrope_SemiBold, FontWeight.SemiBold),
    Font(Res.font.Manrope_Bold, FontWeight.Bold),
    Font(Res.font.Manrope_ExtraBold, FontWeight.ExtraBold),
)


@Composable
fun appTypography(): Typography {
    val manrope = manropeFontFamily()

    return Typography(
        displayLarge = TextStyle(
            fontFamily = manrope,
            fontWeight = FontWeight.Normal,
            fontSize = 57.sp,
            lineHeight = 64.sp
        ),
        displayMedium = TextStyle(
            fontFamily = manrope,
            fontWeight = FontWeight.Normal,
            fontSize = 45.sp,
            lineHeight = 52.sp
        ),
        displaySmall = TextStyle(
            fontFamily = manrope,
            fontWeight = FontWeight.Normal,
            fontSize = 36.sp,
            lineHeight = 44.sp
        ),
        headlineLarge = TextStyle(
            fontFamily = manrope,
            fontWeight = FontWeight.Normal,
            fontSize = 32.sp,
            lineHeight = 40.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = manrope,
            fontWeight = FontWeight.Normal,
            fontSize = 28.sp,
            lineHeight = 36.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = manrope,
            fontWeight = FontWeight.Normal,
            fontSize = 24.sp,
            lineHeight = 32.sp
        ),
        titleLarge = TextStyle(
            fontFamily = manrope,
            fontWeight = FontWeight.Medium,
            fontSize = 22.sp,
            lineHeight = 28.sp
        ),
        titleMedium = TextStyle(
            fontFamily = manrope,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            lineHeight = 24.sp
        ),
        titleSmall = TextStyle(
            fontFamily = manrope,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = manrope,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = manrope,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp
        ),
        bodySmall = TextStyle(
            fontFamily = manrope,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp
        ),
        labelLarge = TextStyle(
            fontFamily = manrope,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp
        ),
        labelMedium = TextStyle(
            fontFamily = manrope,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp
        ),
        labelSmall = TextStyle(
            fontFamily = manrope,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 16.sp
        ),
    )
}

