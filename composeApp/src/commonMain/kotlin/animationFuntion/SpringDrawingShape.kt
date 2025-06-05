package animationFuntion

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class DotParticle(
    var position: Offset,
    var velocity: Offset,
    val color: Color,
    var radius: Float = 5f
) {
    fun move() {
        position += velocity
    }
}

class Dot {
    var position = Animatable(Offset.Zero, Offset.VectorConverter)
    val color = Color(Random.nextFloat(), Random.nextFloat(), Random.nextFloat(), 1f)
    var target = Offset.Zero
    val trail = mutableListOf<Offset>()

    suspend fun animateToTarget() {
        coroutineScope {
            launch {
                position.animateTo(target, animationSpec = TweenSpec(durationMillis = 3000)) {
                    trail.add(value)
                    if (trail.size > 20) {
                        trail.removeFirst()
                    }
                }
            }
        }
    }

    fun setRandomTarget(canvasSize: Offset) {
        target = Offset(
            x = Random.nextFloat() * canvasSize.x,
            y = Random.nextFloat() * canvasSize.y
        )
    }
}

@Composable
fun SpringDrawingShape() {
    var canvasSize by remember { mutableStateOf(Offset.Zero) }
    val dots = remember { List(10) { Dot() } }
    val coroutineScope = rememberCoroutineScope()
    var particles by remember { mutableStateOf(listOf<DotParticle>()) }
    var tapCount by remember { mutableStateOf(0) }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { canvasSize = Offset(it.width.toFloat(), it.height.toFloat()) }
            .pointerInput(Unit) {
                detectTapGestures {
                    tapCount++
                    dots.forEach { dot ->
                        dot.setRandomTarget(canvasSize)
                        coroutineScope.launch {
                            dot.animateToTarget()
                        }
                    }
                }
            }
    ) {

        particles.forEach { particle ->
            drawCircle(color = particle.color, radius = particle.radius, center = particle.position)
        }

        dots.forEach { dot ->
            if (dot.trail.size > 1) {
                val path = Path().apply {
                    var firstPoint = true
                    dot.trail.forEach { point ->
                        if (firstPoint) {
                            moveTo(point.x, point.y)
                            firstPoint = false
                        } else {
                            lineTo(point.x, point.y)
                        }
                    }
                }
                drawPath(path, color = dot.color, alpha = 0.8f, Stroke(width = 3.dp.toPx()))
            }
            drawCircle(color = dot.color, radius = 20f, center = dot.position.value)
        }
    }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            while (isActive) {
                for (i in dots.indices) {
                    for (j in i + 1 until dots.size) {
                        val dot1 = dots[i]
                        val dot2 = dots[j]
                        val distance = sqrt(
                            (dot1.position.value.x - dot2.position.value.x).pow(2) +
                                    (dot1.position.value.y - dot2.position.value.y).pow(2)
                        )
                        if (distance < 40f) {
                            val newParticle = mutableListOf<DotParticle>()
                            repeat(20) {
                                val angle = Random.nextDouble(0.0, 2 * Math.PI)
                                val speed = Random.nextDouble(2.0, 5.0)
                                newParticle += DotParticle(
                                    position = dot1.position.value,
                                    velocity = Offset(
                                        cos(angle).toFloat() * speed.toFloat(),
                                        sin(angle).toFloat() * speed.toFloat()
                                    ),
                                    color = dot1.color
                                )
                            }
                            particles = newParticle
                        }
                    }
                }
                particles.forEach { it.move() }
                delay(16L)
            }
        }
    }
}