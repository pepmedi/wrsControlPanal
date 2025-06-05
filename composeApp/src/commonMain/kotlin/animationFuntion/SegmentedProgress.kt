package animationFuntion

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max
import kotlin.math.min

@Composable
fun SegmentedArcProgressIndicator(
    progress: Float,
    strokeWidth: Float = 48f,
    maxAngle: Float = 160f,
    gapAngle: Float = 15f,
    backgroundColor: Color = Color(0xffCBE0E8),
    progressColor: Brush = Brush.linearGradient(listOf(Color(0xff83B2D1), Color(0xff106AA7)))
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
    )

    Canvas(modifier = Modifier.size(200.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2 - strokeWidth / 2
        val startAngle = -maxAngle / 2 - 90f
        val segmentAngle = (maxAngle - gapAngle) / 2

        drawSegmentArc(
            center,
            radius,
            strokeWidth,
            startAngle,
            segmentAngle,
            color = backgroundColor
        )
        drawSegmentArc(
            center,
            radius,
            strokeWidth,
            startAngle + segmentAngle + gapAngle,
            segmentAngle,
            color = backgroundColor
        )

        drawSegmentArc(
            center, radius, strokeWidth, startAngle,
            segmentAngle * min(animatedProgress * 2, 1f), brush = progressColor
        )
        drawSegmentArc(
            center, radius, strokeWidth, startAngle + segmentAngle + gapAngle,
            segmentAngle * max((animatedProgress - 0.5f) * 2, 0f), brush = progressColor
        )
    }

    Box(
        modifier = Modifier
            .size(200.dp)
            .padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${(progress * 100).toInt()}%",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}

private fun DrawScope.drawSegmentArc(
    center: Offset,
    radius: Float,
    strokeWidth: Float,
    startAngle: Float,
    sweepAngle: Float,
    color: Color? = null,
    brush: Brush? = null
) {
    if (brush != null) {
        drawArc(
            brush = brush,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    } else if (color != null) {
        drawArc(
            color = color,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}


@Composable
fun SegmentedArcProgressScreen() {
    var progress by remember { mutableFloatStateOf(0.75f) }
    var strokeWidth by remember { mutableFloatStateOf(48f) }
    var gapAngle by remember { mutableFloatStateOf(15f) }
    var maxAngle by remember { mutableFloatStateOf(160f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        SegmentedArcProgressIndicator(
            progress = progress,
            maxAngle = maxAngle,
            strokeWidth = strokeWidth,
            gapAngle = gapAngle
        )

        Spacer(modifier = Modifier.height(20.dp))

        SliderWithLabel(
            "Progress: ${(progress * 100).toInt()}%",
            progress,
            { progress = it },
            0f..1f
        )
        SliderWithLabel(
            "Stroke Width: ${strokeWidth.toInt()}",
            strokeWidth,
            { strokeWidth = it },
            10f..80f
        )
        SliderWithLabel("Gap Angle: ${gapAngle.toInt()}°", gapAngle, { gapAngle = it }, 0f..30f)
        SliderWithLabel("Max Angle: ${maxAngle.toInt()}°", maxAngle, { maxAngle = it }, 90f..360f)
    }
}

@Composable
fun SliderWithLabel(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val color = Color(0xff83B2D1)
        Text(text = label, fontSize = 16.sp, fontWeight = FontWeight.Normal)
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.padding(horizontal = 16.dp),
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color,
                inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
            )
        )
    }
}