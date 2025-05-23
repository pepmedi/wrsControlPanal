package theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.wearespine.`in`.theme.AppShapes

@Composable
fun AppTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (useDarkTheme) AppDarkColorScheme() else AppColorScheme()
    MaterialTheme(
        colorScheme = colors,
        shapes = AppShapes,
        content = content
    )
}

object AppTheme {
    val typography: AppTypography = AppTypography()
}