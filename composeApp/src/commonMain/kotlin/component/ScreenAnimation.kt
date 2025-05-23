package component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
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
            modifier = Modifier.fillMaxSize(),
            tonalElevation = 4.dp,
            shape = RoundedCornerShape(12.dp)
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


@Composable
fun SlideInScreenV2(
    visible: Boolean,
    fromLeft: Boolean = false,
    animationDuration: Int = 300,
    elevation: Dp = 8.dp,
    cornerRadius: Dp = 12.dp,
    dimBackground: Boolean = true,
    dismissOnBackgroundClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(
            initialOffsetX = { fullWidth -> if (fromLeft) -fullWidth else fullWidth },
            animationSpec = tween(durationMillis = animationDuration, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(animationDuration)),
        exit = slideOutHorizontally(
            targetOffsetX = { fullWidth -> if (fromLeft) -fullWidth else fullWidth },
            animationSpec = tween(durationMillis = animationDuration, easing = FastOutSlowInEasing)
        ) + fadeOut(animationSpec = tween(animationDuration))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = if (dimBackground) 0.4f else 0f))
                .clickable(
                    onClick = { dismissOnBackgroundClick?.invoke() },
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                )
        ) {
            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.95f),
                tonalElevation = elevation,
                shape = RoundedCornerShape(cornerRadius),
                shadowElevation = elevation
            ) {
                content()
            }
        }
    }
}

@Composable
fun SlideInBottomSheetV2(
    visible: Boolean,
    animationDuration: Int = 300,
    elevation: Dp = 8.dp,
    cornerRadius: Dp = 16.dp,
    dimBackground: Boolean = true,
    dismissOnBackgroundClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { fullHeight -> fullHeight }, // from bottom
            animationSpec = tween(durationMillis = animationDuration, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(animationDuration)),
        exit = slideOutVertically(
            targetOffsetY = { fullHeight -> fullHeight }, // to bottom
            animationSpec = tween(durationMillis = animationDuration, easing = FastOutSlowInEasing)
        ) + fadeOut(animationSpec = tween(animationDuration))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = if (dimBackground) 0.4f else 0f))
                .clickable(
                    onClick = { dismissOnBackgroundClick?.invoke() },
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                )
        ) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(), // full height
                tonalElevation = elevation,
                shape = RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius),
                shadowElevation = elevation,
                color = Color.Transparent
            ) {
                content()
            }
        }
    }
}



