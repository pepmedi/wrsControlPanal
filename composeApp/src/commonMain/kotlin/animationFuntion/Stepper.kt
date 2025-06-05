package animationFuntion

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object StepperDefaults {
    val PillWidth = 80.dp
    val PillHeight = 36.dp
    val ThresholdDp = 24.dp

    val CornerRadius = 18.dp
    val ShadowElevation = 4.dp
    val HorizontalPadding = 12.dp

    const val RotationDivider = 10f
    const val MaxIconRotation = 45f

    val SpringDamping = Spring.DampingRatioMediumBouncy
    val SpringStiffness = Spring.StiffnessMedium

    val GradientStart = Color(0xFF42A5F5) // Blue
    val GradientEnd = Color(0xFF1E88E5)   // Darker blue
    val IconSize = 20.dp
    val IconTint = Color.White
}


@Composable
fun Stepper(
    modifier: Modifier = Modifier,
    initialValue: Int = 16,
    onValueChange: (Int) -> Unit = {}
) {
    var value by remember { mutableIntStateOf(initialValue) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    val thresholdPx = with(LocalDensity.current) {
        StepperDefaults.ThresholdDp.toPx()
    }

    val animatedOffset by animateFloatAsState(
        targetValue = if (isDragging) dragOffset else 0f,
        animationSpec = spring(
            dampingRatio = StepperDefaults.SpringDamping,
            stiffness = StepperDefaults.SpringStiffness
        )
    )

    val iconRotation by animateFloatAsState(
        targetValue = if (isDragging) {
            (dragOffset / thresholdPx * StepperDefaults.MaxIconRotation)
                .coerceIn(-StepperDefaults.MaxIconRotation, StepperDefaults.MaxIconRotation)
        } else 0f,
        animationSpec = spring()
    )

    Box(
        modifier
            .size(StepperDefaults.PillWidth, StepperDefaults.PillHeight)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isDragging = true },
                    onDrag = { ch, delta ->
                        if (ch.positionChange() != Offset.Zero) ch.consume()
                        dragOffset += delta.y
                    },
                    onDragEnd = {
                        when {
                            dragOffset <= -thresholdPx -> {
                                value++; onValueChange(value)
                            }

                            dragOffset >= thresholdPx -> {
                                value--; onValueChange(value)
                            }
                        }
                        dragOffset = 0f
                        isDragging = false
                    }
                )
            }
            .graphicsLayer {
                transformOrigin = TransformOrigin(0f, 0.5f)
                rotationZ = animatedOffset / StepperDefaults.RotationDivider
            }
            .shadow(
                elevation = StepperDefaults.ShadowElevation,
                shape = RoundedCornerShape(StepperDefaults.CornerRadius),
                clip = false
            )
            .background(
                brush = Brush.horizontalGradient(
                    listOf(StepperDefaults.GradientStart, StepperDefaults.GradientEnd)
                )
            )
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(horizontal = StepperDefaults.HorizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AnimatedContent(
                targetState = value,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInVertically { it } + fadeIn() togetherWith
                                slideOutVertically { -it } + fadeOut()
                    } else {
                        slideInVertically { -it } + fadeIn() togetherWith
                                slideOutVertically { it } + fadeOut()
                    }.using(SizeTransform(clip = false))
                }
            ) { target ->
                Text(
                    text = target.toString(),
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Icon(
                imageVector = Icons.Default.ThumbUp,
                contentDescription = null,
                modifier = Modifier
                    .size(StepperDefaults.IconSize)
                    .graphicsLayer { rotationZ = iconRotation },
                tint = StepperDefaults.IconTint
            )
        }
    }
}