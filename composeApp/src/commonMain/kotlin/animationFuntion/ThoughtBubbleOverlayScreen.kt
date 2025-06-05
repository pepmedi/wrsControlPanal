package animationFuntion

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import wrscontrolpanel.composeapp.generated.resources.Res
import wrscontrolpanel.composeapp.generated.resources.visibility

@Composable
fun ThoughtBubbleOverlayScreen() {
    var thoughtText by remember { mutableStateOf("Funny holiday movie for the family? 🍕❤️") }
    var yOffset by remember { mutableFloatStateOf(0f) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
    ) {
        ThoughtBubbleOverlay(
            photoResId = Res.drawable.visibility,
            thoughtText = thoughtText,
            yOffset = yOffset
        )

        Spacer(modifier = Modifier.height(16.dp))

        Slider(
            value = yOffset,
            onValueChange = { yOffset = it },
            valueRange = -150f..150f,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = thoughtText,
            onValueChange = { thoughtText = it },
            label = { Text("Enter your thought") },
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
fun ThoughtBubbleOverlay(photoResId: DrawableResource, thoughtText: String, yOffset: Float) {
    var isClicked by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isClicked) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    Box(modifier = Modifier, contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(photoResId),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .offset(y = (-100).dp + yOffset.dp)
                .animateContentSize(animationSpec = spring())
                .scale(scale)
                .clickable {
                    isClicked = !isClicked
                }
        ) {
            Box(
                modifier = Modifier
                    .width(200.dp + scale.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .shadow(2.dp, RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Text(
                    text = thoughtText,
                    fontSize = 18.sp,
                    color = Color.Black,
                    style = TextStyle(fontSize = 18.sp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .offset(x = (60).dp, y = (-15).dp)
                    .clip(CircleShape)
                    .shadow(2.dp, CircleShape)
                    .background(Color.White)
            )

            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .offset(x = (40).dp, y = -15.dp)
                    .clip(CircleShape)
                    .shadow(2.dp, CircleShape)
                    .background(Color.White)
            )
        }
    }
}