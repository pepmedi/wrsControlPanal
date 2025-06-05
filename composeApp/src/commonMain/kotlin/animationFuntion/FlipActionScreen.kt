package animationFuntion

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FlipActionScreen() {
    var flippedState by remember { mutableStateOf(false) }
    val rotationY by animateFloatAsState(
        targetValue = if (flippedState) 180f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessVeryLow
        )
    )
    val animatedColorAnim by animateColorAsState(
        targetValue = if (flippedState) Color(0xff4C3B4D) else Color(0XFFA53860),
        animationSpec = spring(Spring.DampingRatioMediumBouncy), label = "animatedColorAnim"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .size(250.dp, 400.dp)
                .clip(RoundedCornerShape(20.dp))
                .padding(20.dp)
                .graphicsLayer {
                    this.rotationY = rotationY
                    this.cameraDistance = 12f * density
                }
                .shadow(4.dp, RoundedCornerShape(20.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(animatedColorAnim, animatedColorAnim)
                    )
                )
        ) {
            Text(
                text = if (flippedState) "AC" else "TION",
                fontSize = 24.sp,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .graphicsLayer {
                        this.rotationY = if (rotationY > 90f && rotationY < 270f) 180f else 0f
                    }
            )
        }

        Button(
            onClick = { flippedState = !flippedState },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 30.dp)
        ) {
            Text("Flip")
        }
    }
}