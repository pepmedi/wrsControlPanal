package animationFuntion

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun FloatingHeartsAnimation() {
    var showHearts by remember { mutableStateOf(false) }
    val heartList = remember { mutableStateListOf<Int>() }
    var sliderValue by remember { mutableFloatStateOf(0.5f) }
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    val config = HeartConfig(
        radiusMultiplier = lerp(0.5f, 2f, sliderValue),
        delayDuration = lerp(100L, 600L, sliderValue)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Red),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
            heartList.forEach { id ->
                FloatingHeart(
                    key = id,
                    config = config,
                    onAnimationEnd = { heartList.remove(id) }
                )
            }

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .graphicsLayer {
                        scaleX = scale.value
                        scaleY = scale.value
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        scope.launch {
                            scale.animateTo(0.85f, tween(100, easing = LinearEasing))
                            scale.animateTo(
                                1f,
                                spring(dampingRatio = Spring.DampingRatioHighBouncy)
                            )
                        }

                        showHearts = true
                        repeat(7) { index ->
                            scope.launch {
                                delay(index * config.delayDuration)
                                heartList.add(index + Random.nextInt(1000))
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (showHearts) "Thank you!" else "Press L for love",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(32.dp))

        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(
                thumbColor = Color.Black,
                activeTrackColor = Color.Black
            ),
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

data class HeartConfig(
    val radiusMultiplier: Float,
    val delayDuration: Long
)

@Composable
fun FloatingHeart(
    key: Int,
    config: HeartConfig,
    onAnimationEnd: () -> Unit
) {
    val angle = remember { Random.nextDouble(-90.0, 180.0) }
    val baseRadius = remember { Random.nextDouble(100.0, 500.0).toFloat() }
    val radius = baseRadius * config.radiusMultiplier

    val xOffset = remember { Animatable(0f) }
    val yOffset = remember { Animatable(0f) }
    val alpha = remember { Animatable(0.5f) }

    LaunchedEffect(key1 = key) {
        launch {
            xOffset.animateTo(
                targetValue = (radius * cos(Math.toRadians(angle))).toFloat(),
                animationSpec = tween(durationMillis = 1400, easing = LinearEasing)
            )
        }
        launch {
            yOffset.animateTo(
                targetValue = (radius * -sin(Math.toRadians(angle))).toFloat() - 250f,
                animationSpec = tween(durationMillis = 1400, easing = LinearEasing)
            )
        }
        launch {
            alpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 1400)
            )
            // onAnimationEnd() 🐛there is a break when the animation is terminated.
        }
    }

    Icon(
        imageVector = Icons.Default.Favorite,
        contentDescription = null,
        tint = Color.White.copy(alpha = alpha.value),
        modifier = Modifier
            .offset {
                IntOffset(xOffset.value.roundToInt(), yOffset.value.roundToInt() - 40)
            }
            .size(32.dp)
    )
}