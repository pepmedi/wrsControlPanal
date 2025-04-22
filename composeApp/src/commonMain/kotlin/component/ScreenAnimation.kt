package component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
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

@Composable
fun SlideInBottomSheet(
    visible: Boolean,
    animationDuration: Int = 300,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { fullHeight -> fullHeight }, // Start from below the screen
            animationSpec = tween(durationMillis = animationDuration)
        ),
        exit = slideOutVertically(
            targetOffsetY = { fullHeight -> fullHeight }, // Exit down
            animationSpec = tween(durationMillis = animationDuration)
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            tonalElevation = 4.dp,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp) // For bottom sheet look
        ) {
            content()
        }
    }
}

