package animationFuntion

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CheckboxSwitcher(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    shadowElevation: Dp = 12.dp,
    scalePressed: Float = 0.93f,
    iconSize: Dp = 24.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) scalePressed else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    val transition = updateTransition(targetState = checked, label = "CheckboxTransition")

    val knobOffset by transition.animateDp(label = "Offset") { isChecked ->
        if (isChecked) 52.dp else 6.dp
    }

    val knobColor by transition.animateColor(label = "KnobColor") { isChecked ->
        if (isChecked) Color(0xFF4CAF50) else Color(0xFFE0E0E0)
    }

    val iconAlpha by transition.animateFloat(label = "IconAlpha") { isChecked ->
        if (isChecked) 1f else 0f
    }

    Box(
        modifier = modifier
            .size(width = 112.dp, height = 56.dp)
            .scale(scale)
            .shadow(shadowElevation, shape = RoundedCornerShape(cornerRadius), clip = false)
            .clip(RoundedCornerShape(cornerRadius))
            .background(Color.White)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onCheckedChange(!checked) }
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .padding(6.dp)
                .offset(x = knobOffset)
                .size(44.dp)
                .clip(RoundedCornerShape(cornerRadius / 1.5f))
                .background(knobColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Check",
                tint = Color.White,
                modifier = Modifier
                    .size(iconSize)
                    .alpha(iconAlpha)
            )
        }
    }
}

@Composable
fun CheckBoxSwitcherComponent() {
    var isChecked by remember { mutableStateOf(false) }
    var cornerRadius by remember { mutableStateOf(16f) }
    var shadowElevation by remember { mutableStateOf(12f) }
    var scaleFactor by remember { mutableStateOf(0.93f) }
    var iconSize by remember { mutableStateOf(24f) }

    val sliderColors = SliderDefaults.colors(
        thumbColor = Color(0xFF4CAF50),
        activeTrackColor = Color(0xFF4CAF50),
        inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer,
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F0F0))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CheckboxSwitcher(
            checked = isChecked,
            onCheckedChange = { isChecked = it },
            modifier = Modifier,
            cornerRadius = cornerRadius.dp,
            shadowElevation = shadowElevation.dp,
            scalePressed = scaleFactor,
            iconSize = iconSize.dp
        )

        Spacer(modifier = Modifier.height(120.dp))

        Text("Corner Radius: ${cornerRadius.toInt()} dp")
        Slider(
            colors = sliderColors,
            value = cornerRadius,
            onValueChange = { cornerRadius = it },
            valueRange = 0f..32f
        )

        Text("Shadow Elevation: ${shadowElevation.toInt()} dp")
        Slider(
            colors = sliderColors,
            value = shadowElevation,
            onValueChange = { shadowElevation = it },
            valueRange = 0f..24f
        )

        Text("Pressed Scale: ${String.format("%.2f", scaleFactor)}")
        Slider(
            colors = sliderColors,
            value = scaleFactor,
            onValueChange = { scaleFactor = it },
            valueRange = 0.85f..1f
        )

        Text("Icon Size: ${iconSize.toInt()} dp")
        Slider(
            colors = sliderColors,
            value = iconSize,
            onValueChange = { iconSize = it },
            valueRange = 12f..36f
        )
    }
}