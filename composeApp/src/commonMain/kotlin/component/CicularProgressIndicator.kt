package component

import PrimaryAppColor
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun AppCircularProgressIndicator(modifier: Modifier = Modifier, color: Color = PrimaryAppColor) {
    CircularProgressIndicator(modifier = modifier, color = color)
}