package theme


import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import theme.AppColor.Java100
import theme.AppColor.Java20
import theme.AppColor.Java200
import theme.AppColor.Java300
import theme.AppColor.Java400
import theme.AppColor.Java50
import theme.AppColor.Java500
import theme.AppColor.Java800
import theme.AppColor.Outline12
import theme.AppColor.Vam800

// ---- Brand Colors (JAVA Shades) ----
//val Java800 = Color(0xFF202E40)
//val Java500 = Color(0xFF27374D)
//val Java300 = Color(0xFF526B82)
//val Java200 = Color(0xFF9DB2BF)
//val Java100 = Color(0xFFDDE6ED)
//
//// ---- Base Colors ----
//val Vam800 = Color(0xFF06080C)
//val Java400 = Color(0xFF6282AF)
//val Java50 = Color(0xFFF9FAFB)
//val Java20 = Color(0xFFFEFEFE)
//
//// ---- Outline/Surface Variant ----
//val Outline12 = Color(0x1F000000) // 12% Black
//val Background = Java20
//
//// ---- Feedback Colors ----
//val SuccessGreen = Color(0xFF5CB85C)
//val WarningYellow = Color(0xFFE9D502)
val ErrorRed = Color(0xFFE23636)
val Blue = Color(0xFFE007AFF)

@Composable
fun AppColorScheme(): ColorScheme = lightColorScheme(
    primary = Java800,
    onPrimary = Java20,
    primaryContainer = Java300,
    onPrimaryContainer = Java100,

    secondary = Java500,
    onSecondary = Java20,
    secondaryContainer = Java200,
    onSecondaryContainer = Java100,

    tertiary = Java400,
    onTertiary = Java20,
    tertiaryContainer = Java50,
    onTertiaryContainer = Java100,

    background = Java20,
    onBackground = Vam800,
    surface = Java20,
    onSurface = Java800,
    surfaceVariant = Java100,
    onSurfaceVariant = Java300,

    outline = Outline12,
    outlineVariant = Java100,

    error = ErrorRed,
    onError = Java20,
    errorContainer = Color(0xFFFFE5E5),
    onErrorContainer = ErrorRed,

    inverseSurface = Java800,
    inverseOnSurface = Java20,
    inversePrimary = Java500,

    scrim = Color.Black
)

@Composable
fun AppDarkColorScheme(): ColorScheme = darkColorScheme(
    primary = Java800,
    onPrimary = Java20,
    primaryContainer = Java300,
    onPrimaryContainer = Java100,

    secondary = Java500,
    onSecondary = Java20,
    secondaryContainer = Java200,
    onSecondaryContainer = Java100,

    tertiary = Java400,
    onTertiary = Java20,
    tertiaryContainer = Java50,
    onTertiaryContainer = Java100,

    background = Java20,
    onBackground = Vam800,
    surface = Java20,
    onSurface = Java800,
    surfaceVariant = Java100,
    onSurfaceVariant = Java300,

    outline = Outline12,
    outlineVariant = Java100,

    error = ErrorRed,
    onError = Java20,
    errorContainer = ErrorRed,
    onErrorContainer = ErrorRed,

    inverseSurface = Java800,
    inverseOnSurface = Java20,
    inversePrimary = Java500,

    scrim = Color.Black
)

object AppColor {
    val black = Color(0xFF000000)
    val white = Color(0xFFFFFFFF)
    val transparent = Color(0x00000000)

    val black50 = Color(0xFFF9FAFB)
    val black100 = Color(0xFFF3F4F6)
    val black200 = Color(0xFFE5E7EB)
    val black300 = Color(0xFFD1D5DB)
    val black400 = Color(0xFF9CA3AF)
    val black500 = Color(0xFF6B7280)
    val black600 = Color(0xFF4B5563)
    val black700 = Color(0xFF374151)
    val black800 = Color(0xFF1F2937)
    val black900 = Color(0xFF111827)

    val blue50 = Color(0xFFEFF6FF)
    val blue100 = Color(0xFFDBEAFE)
    val blue200 = Color(0xFFBFDBFE)
    val blue300 = Color(0xFF93C5FD)
    val blue400 = Color(0xFF60A5FA)
    val blue500 = Color(0xFF3B82F6)
    val blue600 = Color(0xFF2563EB)
    val blue700 = Color(0xFF1D4ED8)
    val blue800 = Color(0xFF1E40AF)
    val blue900 = Color(0xFF1E3A8A)

    val green50 = Color(0xFFF0FDF4)
    val green100 = Color(0xFFDCFCE7)
    val green200 = Color(0xFFBBF7D0)
    val green300 = Color(0xFF86EFAC)
    val green400 = Color(0xFF4ADE80)
    val green500 = Color(0xFF22C55E)
    val green600 = Color(0xFF16A34A)
    val green700 = Color(0xFF15803D)
    val green800 = Color(0xFF166534)
    val green900 = Color(0xFF14532D)

    val red50 = Color(0xFFFEF2F2)
    val red100 = Color(0xFFFEE2E2)
    val red200 = Color(0xFFFECACA)
    val red300 = Color(0xFFFCA5A5)
    val red400 = Color(0xFFF87171)
    val red500 = Color(0xFFEF4444)
    val red600 = Color(0xFFDC2626)
    val red700 = Color(0xFFB91C1C)
    val red800 = Color(0xFF991B1B)
    val red900 = Color(0xFF7F1D1D)

    val violet50 = Color(0xFFF5F3FF)
    val violet100 = Color(0xFFEDE9FE)
    val violet500 = Color(0xFF8B5CF6)
    val violet600 = Color(0xFF7C3AED)
    val violet700 = Color(0xFF6D28D9)

    val yellow50 = Color(0xFFFEFCE8)
    val yellow100 = Color(0xFFFEF9C3)
    val yellow200 = Color(0xFFFEF08A)
    val yellow300 = Color(0xFFFDE047)
    val yellow400 = Color(0xFFFACC15)
    val yellow500 = Color(0xFFEAB308)
    val yellow600 = Color(0xFFCA8A04)
    val yellow700 = Color(0xFFA16207)
    val yellow800 = Color(0xFF854D0E)
    val yellow900 = Color(0xFF713F12)

    val fuscia50 = Color(0xFFFDF4FF)

    val Java800 = Color(0xFF202E40)
    val Java500 = Color(0xFF27374D)
    val Java300 = Color(0xFF526B82)
    val Java200 = Color(0xFF9DB2BF)
    val Java100 = Color(0xFFDDE6ED)

    // ---- Base Colors ----
    val Vam800 = Color(0xFF06080C)
    val Java400 = Color(0xFF6282AF)
    val Java50 = Color(0xFFF9FAFB)
    val Java20 = Color(0xFFFEFEFE)

    // ---- Outline/Surface Variant ----
    val Outline12 = Color(0x1F000000) // 12% Black
    val Background = Java20

    // ---- Feedback Colors ----
    val SuccessGreen = Color(0xFF5CB85C)
    val WarningYellow = Color(0xFFE9D502)
    val ErrorRed = Color(0xFFE23636)
    val Blue = Color(0xFFE007AFF)

}