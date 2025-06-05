package animationFuntion

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.hypot

@Composable
fun SliceMenu(modifier: Modifier = Modifier, onSliceClick: (Int) -> Unit = {}) {
    val slices = 6
    val colors = listOf(
        Brush.linearGradient(listOf(Color(0xFFFF1744), Color(0xFFFFC400))),
        Brush.linearGradient(listOf(Color(0xFF1A237E), Color(0xFF2962FF))),
        Brush.linearGradient(listOf(Color(0xFF00C853), Color(0xFF64DD17))),
        Brush.linearGradient(listOf(Color(0xFFFF6D00), Color(0xFFFFAB00))),
        Brush.linearGradient(listOf(Color(0xFFD500F9), Color(0xFF6200EA))),
        Brush.linearGradient(listOf(Color(0xFF00BFA5), Color(0xFF00E5FF)))
    )
    val degreeStep = 360f / slices
    var innerRadiusRatio by remember { mutableStateOf(0.3f) }

    var visibleSlices by remember { mutableStateOf(0) }
    var sliceSize by remember { mutableStateOf(1.0f) }
    val coroutineScope = rememberCoroutineScope()

    val animatedValues = remember { List(slices) { Animatable(0f) } }
    val sliceClickAnimations = remember { List(slices) { Animatable(1f) } }

    Column(modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
        Box(modifier = Modifier.weight(1f)) {
            Canvas(
                modifier = Modifier
                    .padding(start = 60.dp, end = 60.dp)
                    .fillMaxSize()
                    .pointerInput(true) {
                        detectTapGestures(
                            onTap = { tapOffset ->
                                val center = Offset(x = size.width / 2f, y = size.height / 2f)
                                val dx = tapOffset.x - center.x
                                val dy = tapOffset.y - center.y
                                val distance = hypot(dx, dy)

                                val outerRadius = minOf(center.x, center.y)
                                val innerRadius = outerRadius * innerRadiusRatio

                                if (distance < innerRadius) {

                                    coroutineScope.launch {
                                        for (i in 0 until slices) {
                                            visibleSlices = i + 1
                                            animatedValues[i].animateTo(
                                                targetValue = 1.0f,
                                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                                            )
                                            delay(200)
                                        }
                                    }
                                } else if (distance > innerRadius && distance < outerRadius && visibleSlices == slices) {
                                    val angle = (atan2(dy, dx) * (180 / PI)).toFloat() + 180
                                    val sliceIndex = ((angle / degreeStep).toInt()) % slices
                                    onSliceClick(sliceIndex)


                                    coroutineScope.launch {
                                        sliceClickAnimations[sliceIndex].animateTo(
                                            targetValue = 1.2f,
                                            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
                                        )
                                        sliceClickAnimations[sliceIndex].animateTo(
                                            targetValue = 1f,
                                            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
                                        )
                                    }
                                }
                            }
                        )
                    },
                onDraw = {
                    val center = Offset(x = size.width / 2f, y = size.height / 2f)
                    val outerRadius = minOf(center.x, center.y) * sliceSize
                    val innerRadius = outerRadius * innerRadiusRatio

                    var startAngle = -90f


                    for (i in 0 until visibleSlices) {
                        val animNormalized = animatedValues[i].value
                        val animatedSweepAngle = animNormalized * degreeStep

                        val scaleValue = sliceClickAnimations[i].value

                        scale(scale = scaleValue, pivot = center) {
                            drawArc(
                                brush = colors[i % colors.size],
                                startAngle = startAngle,
                                sweepAngle = animatedSweepAngle,
                                useCenter = true,
                                topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                                size = Size(
                                    outerRadius * 2,
                                    outerRadius * 2
                                )
                            )

                            drawArc(
                                color = Color.White,
                                startAngle = startAngle,
                                sweepAngle = animatedSweepAngle,
                                useCenter = true,
                                topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                                size = Size(outerRadius * 2, outerRadius * 2),
                                style = Stroke(width = 10f)
                            )
                        }

                        startAngle += degreeStep
                    }

                    drawCircle(
                        color = Color.Black,
                        radius = innerRadius,
                        center = center
                    )
                    drawCircle(
                        color = Color.White,
                        radius = innerRadius,
                        center = center,
                        style = Stroke(width = 10f)
                    )
                }
            )
        }

        Slider(
            value = sliceSize,
            onValueChange = { sliceSize = it },
            valueRange = 0.5f..1.5f,
            modifier = Modifier.padding(16.dp)
        )

        Slider(
            value = innerRadiusRatio,
            onValueChange = { innerRadiusRatio = it },
            valueRange = 0.1f..0.5f,
            modifier = Modifier.padding(16.dp)
        )
    }
}