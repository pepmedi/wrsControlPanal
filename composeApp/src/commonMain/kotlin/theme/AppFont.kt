package theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import wrscontrolpanel.composeapp.generated.resources.Manrope_Bold
import wrscontrolpanel.composeapp.generated.resources.Manrope_Medium
import wrscontrolpanel.composeapp.generated.resources.Manrope_Regular
import wrscontrolpanel.composeapp.generated.resources.Manrope_SemiBold
import wrscontrolpanel.composeapp.generated.resources.Res

@Composable
fun AppFontFamily() =
    FontFamily(
        Font(Res.font.Manrope_Regular, weight = FontWeight.Normal),
        Font(Res.font.Manrope_Medium, weight = FontWeight.Medium),
        Font(Res.font.Manrope_SemiBold, weight = FontWeight.SemiBold),
        Font(Res.font.Manrope_Bold, weight = FontWeight.Bold)
    )

@Composable
private fun Header() = TextStyle.Default.copy(
    fontFamily = AppFontFamily(),
    fontWeight = FontWeight.Bold,
    lineHeight = 0.sp
)

@Composable
private fun Subtitle() = TextStyle.Default.copy(
    fontFamily = AppFontFamily(),
    fontWeight = FontWeight.SemiBold,
    lineHeight = 0.sp
)

@Composable
private fun Body() = TextStyle.Default.copy(
    fontFamily = AppFontFamily(),
    fontWeight = FontWeight.Normal,
    lineHeight = 0.sp
)

@Composable
private fun Caption() = TextStyle.Default.copy(
    fontFamily = AppFontFamily(),
    fontWeight = FontWeight.Normal,
    lineHeight = 0.sp
)

@Composable
private fun OverLine() = TextStyle.Default.copy(
    fontFamily = AppFontFamily(),
    fontWeight = FontWeight.Medium,
    lineHeight = 0.sp
)

@Composable
fun H1(color: Color = Java500) = Header().copy(fontSize = 40.sp, lineHeight = 48.sp, color = color)

@Composable
fun H2(color: Color = Java500) = Header().copy(fontSize = 36.sp, lineHeight = 44.sp, color = color)

@Composable
fun H3(color: Color = Java500) = Header().copy(fontSize = 32.sp, lineHeight = 40.sp, color = color)

@Composable
fun H4(color: Color = Java500) = Header().copy(fontSize = 28.sp, lineHeight = 36.sp, color = color)

@Composable
fun H5(color: Color = Java500) = Header().copy(fontSize = 24.sp, lineHeight = 32.sp, color = color)

@Composable
fun H6(color: Color = Java500) = Header().copy(fontSize = 20.sp, lineHeight = 28.sp, color = color)

// ----------------- Subtitle -----------------

@Composable
fun Subtitle1(color: Color = Java500) =
    Subtitle().copy(fontSize = 16.sp, lineHeight = 24.sp, color = color)

@Composable
fun Subtitle2(color: Color = Java500) =
    Subtitle().copy(fontSize = 14.sp, lineHeight = 22.sp, color = color)

// ----------------- Body -----------------

@Composable
fun Body1(color: Color = Java500) = Body().copy(fontSize = 16.sp, lineHeight = 24.sp, color = color)

@Composable
fun Body2(color: Color = Java500) = Body().copy(fontSize = 14.sp, lineHeight = 22.sp, color = color)

// ----------------- Caption & OverLine -----------------

@Composable
fun CaptionText(color: Color = Java500) =
    Caption().copy(fontSize = 12.sp, lineHeight = 20.sp, letterSpacing = 0.4.sp, color = color)

@Composable
fun CaptionTextSmall(color: Color = Java500) =
    Caption().copy(fontSize = 10.sp, lineHeight = 18.sp, color = color)

@Composable
fun OverLineText(color: Color = Java500) =
    OverLine().copy(fontSize = 10.sp, lineHeight = 18.sp, letterSpacing = 2.sp, color = color)

@Composable
fun BottomNavTextStyle(color: Color = Java500) =
    OverLine().copy(fontSize = 10.sp, lineHeight = 16.sp, letterSpacing = 0.sp, color = color)

// ----------------- Button Text -----------------

@Composable
fun ButtonTextExtraLarge(color: Color = Java500) =
    Body().copy(fontSize = 24.sp, lineHeight = 32.sp, color = color)

@Composable
fun ButtonTextLarge(color: Color = Java500) =
    Body().copy(fontSize = 21.sp, lineHeight = 28.sp, color = color)

@Composable
fun ButtonTextMedium(color: Color = Java500) =
    Body().copy(fontSize = 18.sp, lineHeight = 24.sp, color = color)

@Composable
fun ButtonTextSmall(color: Color = Java500) =
    Body().copy(fontSize = 15.sp, lineHeight = 20.sp, color = color)

@Composable
fun ButtonTextExtraSmall(color: Color = Java500) =
    Body().copy(fontSize = 12.sp, lineHeight = 16.sp, color = color)


@Immutable
class AppTypography {
    private val defaultStyle: TextStyle
        @Composable get() {
            val fontFamily = FontFamily(
                Font(Res.font.Manrope_Regular),
                Font(Res.font.Manrope_Medium, FontWeight.Medium),
                Font(Res.font.Manrope_SemiBold, FontWeight.SemiBold),
                Font(Res.font.Manrope_Bold, FontWeight.Bold)
            )
            return TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                color = AppColor.black900
            )
        }

    val h1: TextStyle
        @Composable get() {
            return defaultStyle.copy(
                fontSize = 24.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.Medium
            )
        }

    val h1Bold: TextStyle
        @Composable get() {
            return h1.copy(
                fontWeight = FontWeight.Bold
            )
        }

    val h1SemiBold: TextStyle
        @Composable get() {
            return h1.copy(
                fontWeight = FontWeight.SemiBold
            )
        }

    val h2: TextStyle
        @Composable get() {
            return defaultStyle.copy(
                fontSize = 20.sp,
                lineHeight = 30.sp,
                fontWeight = FontWeight.Medium
            )
        }

    val h2Bold: TextStyle
        @Composable get() {
            return h2.copy(
                fontWeight = FontWeight.Bold
            )
        }

    val h2SemiBold: TextStyle
        @Composable get() {
            return h2.copy(
                fontWeight = FontWeight.SemiBold
            )
        }

    val h3: TextStyle
        @Composable get() {
            return defaultStyle.copy(
                fontSize = 18.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.Medium
            )
        }

    val h3Bold: TextStyle
        @Composable get() {
            return h3.copy(
                fontWeight = FontWeight.Bold
            )
        }

    val h3SemiBold: TextStyle
        @Composable get() {
            return h3.copy(
                fontWeight = FontWeight.SemiBold
            )
        }

    val h4: TextStyle
        @Composable get() {
            return defaultStyle.copy(
                fontSize = 16.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Medium
            )
        }

    val h4Bold: TextStyle
        @Composable get() {
            return h4.copy(
                fontWeight = FontWeight.Bold
            )
        }

    val h4SemiBold: TextStyle
        @Composable get() {
            return h4.copy(
                fontWeight = FontWeight.SemiBold
            )
        }

    val bodyLarge: TextStyle
        @Composable get() {
            return h4
        }

    val bodyLargeBold: TextStyle
        @Composable get() {
            return h4Bold
        }

    val bodySmall: TextStyle
        @Composable get() {
            return defaultStyle.copy(
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Medium
            )
        }

    val bodySmallBold: TextStyle
        @Composable get() {
            return bodySmall.copy(fontWeight = FontWeight.Bold)
        }

    val subtitle: TextStyle
        @Composable get() {
            return defaultStyle.copy(
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Medium
            )
        }

    val subtitleBold: TextStyle
        @Composable get() {
            return subtitle.copy(fontWeight = FontWeight.Bold)
        }

    val subtitleSemiBold: TextStyle
        @Composable get() {
            return subtitle.copy(fontWeight = FontWeight.SemiBold)
        }

    val subtitleSmall: TextStyle
        @Composable get() {
            return defaultStyle.copy(
                fontSize = 12.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }

    val subtitleSmallBold: TextStyle
        @Composable get() {
            return subtitleSmall.copy(fontWeight = FontWeight.Bold)
        }

    val subtitleExtraSmall: TextStyle
        @Composable get() {
            return defaultStyle.copy(
                fontSize = 10.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }

    val subtitleExtraSmallBold: TextStyle
        @Composable get() {
            return subtitleExtraSmall.copy(fontWeight = FontWeight.Bold)
        }
}