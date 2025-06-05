package animationFuntion

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sin

@Composable
fun CartAnimation() {

    val infiniteTransition = rememberInfiniteTransition()

    var pressed by remember { mutableStateOf(false) }

    val width by animateDpAsState(
        targetValue = if (pressed) 280.dp else 300.dp,
        animationSpec = spring(Spring.DampingRatioMediumBouncy)
    )
    val height by animateDpAsState(
        targetValue = if (pressed) 180.dp else 200.dp,
        animationSpec = spring(Spring.DampingRatioMediumBouncy)
    )
    val amplitude by animateFloatAsState(
        targetValue = if (pressed) 40f else 20f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy)
    )

    val lightEffectPosition by infiniteTransition.animateFloat(
        initialValue = -0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "light Animation"
    )
    val lineColorAnimation by infiniteTransition.animateColor(
        initialValue = Color.Red,
        targetValue = Color.Yellow,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "Wawe Float Animation"
    )

    val linePhaseAnimation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "Wawe Float Animation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(width, height)
                .clip(RoundedCornerShape(10.dp))
                .shadow(4.dp, RoundedCornerShape(10.dp))
                .background(Color.LightGray)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            pressed = true
                            tryAwaitRelease()
                            pressed = false
                        }
                    )
                }
        ) {
            Text(
                text = "🟠🟡",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(16.dp)
            )
            Canvas(modifier = Modifier.fillMaxSize()) {
                val path = androidx.compose.ui.graphics.Path()
                val lightWidth = size.width * 0.2f
                val lightStart = lightEffectPosition * size.width - lightWidth
                val lightEnd = lightStart + lightWidth
                val brush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, Color.White, Color.Transparent),
                    startX = lightStart,
                    endX = lightEnd
                )
                for (x in 0 until size.width.toInt()) {
                    val y =
                        sin((x * 1f + linePhaseAnimation) * kotlin.math.PI / 180) * amplitude + size.height / 2
                    if (x == 0) {
                        path.moveTo(x.toFloat(), y.toFloat())
                    } else {
                        path.lineTo(x.toFloat(), y.toFloat())
                    }
                }

                drawRect(
                    brush = brush,
                    topLeft = Offset(0f, 0f),
                    size = size
                )

                drawPath(path, color = lineColorAnimation, style = Stroke(5.dp.toPx()))
            }
        }
    }
}