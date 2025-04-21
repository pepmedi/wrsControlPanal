package component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SlideInScreen(
    visible: Boolean,
    fromLeft: Boolean = false,
    animationDuration: Int = 300,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(
            initialOffsetX = { fullWidth -> if (fromLeft) -fullWidth else fullWidth },
            animationSpec = tween(durationMillis = animationDuration)
        ),
        exit = slideOutHorizontally(
            targetOffsetX = { fullWidth -> if (fromLeft) -fullWidth else fullWidth },
            animationSpec = tween(durationMillis = animationDuration)
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(), // You can customize width/height here
            tonalElevation = 4.dp,
            shape = RoundedCornerShape(12.dp) // Optional styling
        ) {
            content()
        }
    }
}
