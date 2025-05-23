package theme

import SecondaryAppColor
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.waterfallPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

//// TODO :: Create all button variants
//@Composable
//fun LoadingButton(
//    onClick: () -> Unit,
//    modifier: Modifier = Modifier,
//    text: String,
//    isLoading: Boolean = false,
//    enabled: Boolean = true,
//    buttonTextStyle: TextStyle = ButtonTextMedium().copy(color = Java20),
//    buttonContainerColor: Color = Java500,
//) {
//    val keyboardController = LocalSoftwareKeyboardController.current
//
//    Button(
//        onClick = {
//            keyboardController?.hide()
//            onClick()
//        },
//        modifier = modifier
//            .height(keylineDimen48),
//        shape = AppShapes.medium,
//        interactionSource = remember { MutableInteractionSource() },
//        enabled = enabled && !isLoading,
//        colors = ButtonDefaults.buttonColors( // TODO :: Change button colors
//            containerColor = buttonContainerColor,
//            contentColor = MaterialTheme.colorScheme.onPrimary,
//            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
//            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f)
//        )
//    ) {
//        if (isLoading) {
//            CircularProgressIndicator(
//                modifier = Modifier
//                    .size(keylineDimen20),
//                color = MaterialTheme.colorScheme.onPrimary,
//                strokeWidth = 2.dp
//            )
//        } else {
//            Text(
//                text = text,
//                style = buttonTextStyle,
//                maxLines = 1,
//                overflow = TextOverflow.Ellipsis
//            )
//        }
//    }
//}

enum class ButtonViewState {
    DEFAULT,
    LOADING,
    ERROR
}

enum class ButtonType {
    DEFAULT,
    EXTRA_SMALL,
    SMALL,
    LARGE,
    EXTRA_LARGE,
    OUTLINE,
    EXTRA_SMALL_OUTLINE,
    SMALL_OUTLINE,
    LARGE_OUTLINE,
    EXTRA_LARGE_OUTLINE,
    SECONDARY,
    EXTRA_SMALL_SECONDARY,
    SMALL_SECONDARY,
    LARGE_SECONDARY,
    EXTRA_LARGE_SECONDARY,
    SECONDARY_OUTLINE,
    EXTRA_SMALL_SECONDARY_OUTLINE,
    SMALL_SECONDARY_OUTLINE,
    LARGE_SECONDARY_OUTLINE,
    EXTRA_LARGE_SECONDARY_OUTLINE
}

private fun ButtonType.getCornerRadius(isRounder: Boolean) =
    when (this) {
        ButtonType.DEFAULT, ButtonType.OUTLINE, ButtonType.SECONDARY, ButtonType.SECONDARY_OUTLINE -> if (isRounder) 40.dp else 8.dp
        ButtonType.LARGE, ButtonType.LARGE_OUTLINE, ButtonType.LARGE_SECONDARY, ButtonType.LARGE_SECONDARY_OUTLINE -> if (isRounder) 44.dp else 8.dp
        ButtonType.EXTRA_LARGE, ButtonType.EXTRA_LARGE_OUTLINE, ButtonType.EXTRA_LARGE_SECONDARY, ButtonType.EXTRA_LARGE_SECONDARY_OUTLINE -> if (isRounder) 48.dp else 8.dp
        ButtonType.SMALL, ButtonType.SMALL_OUTLINE, ButtonType.SMALL_SECONDARY, ButtonType.SMALL_SECONDARY_OUTLINE -> if (isRounder) 40.dp else 8.dp
        ButtonType.EXTRA_SMALL, ButtonType.EXTRA_SMALL_OUTLINE, ButtonType.EXTRA_SMALL_SECONDARY, ButtonType.EXTRA_SMALL_SECONDARY_OUTLINE -> if (isRounder) 32.dp else 8.dp
    }


private fun ButtonType.getHeight() =
    when (this) {
        ButtonType.DEFAULT, ButtonType.OUTLINE, ButtonType.SECONDARY, ButtonType.SECONDARY_OUTLINE -> 38.dp
        ButtonType.LARGE, ButtonType.LARGE_OUTLINE, ButtonType.LARGE_SECONDARY, ButtonType.LARGE_SECONDARY_OUTLINE -> 44.dp
        ButtonType.EXTRA_LARGE, ButtonType.EXTRA_LARGE_OUTLINE, ButtonType.EXTRA_LARGE_SECONDARY, ButtonType.EXTRA_LARGE_SECONDARY_OUTLINE -> 48.dp
        ButtonType.SMALL, ButtonType.SMALL_OUTLINE, ButtonType.SMALL_SECONDARY, ButtonType.SMALL_SECONDARY_OUTLINE -> 32.dp
        ButtonType.EXTRA_SMALL, ButtonType.EXTRA_SMALL_OUTLINE, ButtonType.EXTRA_SMALL_SECONDARY, ButtonType.EXTRA_SMALL_SECONDARY_OUTLINE -> 28.dp
    }


private fun ButtonType.getIconSize() =
    when (this) {
        ButtonType.DEFAULT, ButtonType.OUTLINE, ButtonType.SECONDARY, ButtonType.SECONDARY_OUTLINE -> 24.dp
        ButtonType.LARGE, ButtonType.LARGE_OUTLINE, ButtonType.LARGE_SECONDARY, ButtonType.LARGE_SECONDARY_OUTLINE -> 24.dp
        ButtonType.EXTRA_LARGE, ButtonType.EXTRA_LARGE_OUTLINE, ButtonType.EXTRA_LARGE_SECONDARY, ButtonType.EXTRA_LARGE_SECONDARY_OUTLINE -> 24.dp
        ButtonType.SMALL, ButtonType.SMALL_OUTLINE, ButtonType.SMALL_SECONDARY, ButtonType.SMALL_SECONDARY_OUTLINE -> 16.dp
        ButtonType.EXTRA_SMALL, ButtonType.EXTRA_SMALL_OUTLINE, ButtonType.EXTRA_SMALL_SECONDARY, ButtonType.EXTRA_SMALL_SECONDARY_OUTLINE -> 16.dp
    }


@Composable
private fun ButtonType.getTextStyle() =
    when (this) {
        ButtonType.DEFAULT, ButtonType.OUTLINE, ButtonType.SECONDARY, ButtonType.SECONDARY_OUTLINE -> AppTheme.typography.subtitleBold
        ButtonType.LARGE, ButtonType.LARGE_OUTLINE, ButtonType.LARGE_SECONDARY, ButtonType.LARGE_SECONDARY_OUTLINE -> AppTheme.typography.h4Bold
        ButtonType.EXTRA_LARGE, ButtonType.EXTRA_LARGE_OUTLINE, ButtonType.EXTRA_LARGE_SECONDARY, ButtonType.EXTRA_LARGE_SECONDARY_OUTLINE -> AppTheme.typography.h4Bold
        ButtonType.SMALL, ButtonType.SMALL_OUTLINE, ButtonType.SMALL_SECONDARY, ButtonType.SMALL_SECONDARY_OUTLINE -> AppTheme.typography.subtitleBold
        ButtonType.EXTRA_SMALL, ButtonType.EXTRA_SMALL_OUTLINE, ButtonType.EXTRA_SMALL_SECONDARY, ButtonType.EXTRA_SMALL_SECONDARY_OUTLINE -> AppTheme.typography.subtitleSmallBold
    }

private fun ButtonType.getBackgroundTint() =
    when (this) {
        ButtonType.DEFAULT, ButtonType.LARGE, ButtonType.EXTRA_LARGE, ButtonType.SMALL, ButtonType.EXTRA_SMALL ->  SecondaryAppColor //AppColor.Java500
        ButtonType.OUTLINE, ButtonType.LARGE_OUTLINE, ButtonType.EXTRA_LARGE_OUTLINE, ButtonType.SMALL_OUTLINE, ButtonType.EXTRA_SMALL_OUTLINE -> AppColor.white
        ButtonType.SECONDARY, ButtonType.LARGE_SECONDARY, ButtonType.EXTRA_LARGE_SECONDARY, ButtonType.SMALL_SECONDARY, ButtonType.EXTRA_SMALL_SECONDARY -> AppColor.Java100
        ButtonType.SECONDARY_OUTLINE, ButtonType.LARGE_SECONDARY_OUTLINE, ButtonType.EXTRA_LARGE_SECONDARY_OUTLINE, ButtonType.SMALL_SECONDARY_OUTLINE, ButtonType.EXTRA_SMALL_SECONDARY_OUTLINE -> AppColor.Java20
    }

private fun ButtonType.getTextColor() =
    when (this) {
        ButtonType.DEFAULT, ButtonType.LARGE, ButtonType.EXTRA_LARGE, ButtonType.SMALL, ButtonType.EXTRA_SMALL -> AppColor.white
        ButtonType.SECONDARY, ButtonType.LARGE_SECONDARY, ButtonType.EXTRA_LARGE_SECONDARY, ButtonType.SMALL_SECONDARY, ButtonType.EXTRA_SMALL_SECONDARY -> AppColor.Java500
        ButtonType.SECONDARY_OUTLINE, ButtonType.LARGE_SECONDARY_OUTLINE, ButtonType.EXTRA_LARGE_SECONDARY_OUTLINE, ButtonType.SMALL_SECONDARY_OUTLINE, ButtonType.EXTRA_SMALL_SECONDARY_OUTLINE -> AppColor.Java300
        ButtonType.OUTLINE, ButtonType.LARGE_OUTLINE, ButtonType.EXTRA_LARGE_OUTLINE, ButtonType.SMALL_OUTLINE, ButtonType.EXTRA_SMALL_OUTLINE -> AppColor.blue600
    }

private fun ButtonType.getBorderStroke(enabled: Boolean) =
    when (this) {
        ButtonType.DEFAULT, ButtonType.LARGE, ButtonType.EXTRA_LARGE, ButtonType.SMALL, ButtonType.EXTRA_SMALL, ButtonType.SECONDARY, ButtonType.LARGE_SECONDARY, ButtonType.EXTRA_LARGE_SECONDARY, ButtonType.SMALL_SECONDARY, ButtonType.EXTRA_SMALL_SECONDARY -> null
        ButtonType.OUTLINE, ButtonType.LARGE_OUTLINE, ButtonType.EXTRA_LARGE_OUTLINE, ButtonType.SMALL_OUTLINE, ButtonType.EXTRA_SMALL_OUTLINE, ButtonType.SECONDARY_OUTLINE, ButtonType.LARGE_SECONDARY_OUTLINE, ButtonType.EXTRA_LARGE_SECONDARY_OUTLINE, ButtonType.SMALL_SECONDARY_OUTLINE, ButtonType.EXTRA_SMALL_SECONDARY_OUTLINE -> BorderStroke(
            1.dp,
            if (enabled) AppColor.Java500 else AppColor.Java300
        )
    }

@OptIn(ExperimentalMaterial3Api::class)
private fun ButtonType.getRippleTheme() =
    when (this) {
        ButtonType.DEFAULT, ButtonType.LARGE, ButtonType.EXTRA_LARGE, ButtonType.SMALL, ButtonType.EXTRA_SMALL -> primaryButtonGroupRippleTheme
        ButtonType.OUTLINE, ButtonType.LARGE_OUTLINE, ButtonType.EXTRA_LARGE_OUTLINE, ButtonType.SMALL_OUTLINE, ButtonType.EXTRA_SMALL_OUTLINE -> outlineButtonGroupRippleTheme
        ButtonType.SECONDARY, ButtonType.LARGE_SECONDARY, ButtonType.EXTRA_LARGE_SECONDARY, ButtonType.SMALL_SECONDARY, ButtonType.EXTRA_SMALL_SECONDARY -> secondaryButtonGroupRippleTheme
        ButtonType.SECONDARY_OUTLINE, ButtonType.LARGE_SECONDARY_OUTLINE, ButtonType.EXTRA_LARGE_SECONDARY_OUTLINE, ButtonType.SMALL_SECONDARY_OUTLINE, ButtonType.EXTRA_SMALL_SECONDARY_OUTLINE -> outlineButtonGroupRippleTheme
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ComposeButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    text: String,
    enabled: Boolean = true,
    iconResource: DrawableResource? = null,
    iconTint: Color? = null,
    iconSize: Dp,
    textStyle: TextStyle,
    cornerRadius: Dp,
    height: Dp,
    buttonTint: Color,
    textColor: Color,
    disabledBackgroundTint: Color,
    disabledTextColor: Color,
    borderStroke: BorderStroke?,
    rippleTheme: RippleConfiguration,
    viewState: ButtonViewState = ButtonViewState.DEFAULT,
) {
    CompositionLocalProvider(LocalRippleConfiguration provides rippleTheme) {
        Button(
            onClick = {
                onClick.invoke()
            },
            enabled = enabled && viewState != ButtonViewState.LOADING,
            modifier = modifier.then(
                Modifier
                    .height(height)
                    .requiredHeight(height)
                    .defaultMinSize(minHeight = height)
                    .padding(all = 0.dp)
            ),
            content = {
                iconResource?.let {
                    Image(
                        painter = painterResource(resource = iconResource),
                        contentDescription = "primary button icon",
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(iconSize),
                        colorFilter = iconTint?.let { ColorFilter.tint(iconTint) }
                    )
                }
                Box(contentAlignment = Alignment.Center) {
                    val loading = if (viewState == ButtonViewState.LOADING) 1f else 0f
                    Text(
                        text = text,
                        style = textStyle.copy(
                            if (enabled && viewState != ButtonViewState.LOADING) textColor else disabledTextColor
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(all = 0.dp)
                            .waterfallPadding()
                            .alpha(1f - loading)
                    )
                    if (loading == 1f)
                        CircularProgressIndicator(
                            modifier = Modifier.size((height * 0.7f)),
                            color = AppColor.white,
                            strokeWidth = 3.dp
                        )
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonTint,
                contentColor = textColor,
                disabledContainerColor = disabledBackgroundTint,
                disabledContentColor = disabledTextColor
            ),
            shape = RoundedCornerShape(cornerRadius),
            contentPadding = PaddingValues(
                start = 24.dp,
                top = 0.dp,
                end = 24.dp,
                bottom = 0.dp
            ),
            border = borderStroke,
            interactionSource = remember { MutableInteractionSource() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppButton(
    modifier: Modifier = Modifier,
    buttonType: ButtonType = ButtonType.DEFAULT,
    text: String,
    enabled: Boolean = true,
    rounded: Boolean = false,
    iconResource: DrawableResource? = null,
    iconTint: Color? = null,
    viewState: ButtonViewState = ButtonViewState.DEFAULT,
    onClick: () -> Unit,
) = ComposeButton(
    modifier = modifier,
    onClick = onClick,
    text = text,
    enabled = enabled,
    iconResource = iconResource,
    iconTint = iconTint,
    iconSize = buttonType.getIconSize(),
    textStyle = buttonType.getTextStyle().copy(color = buttonType.getTextColor()),
    height = buttonType.getHeight(),
    cornerRadius = buttonType.getCornerRadius(rounded),
    buttonTint = buttonType.getBackgroundTint(),
    textColor = buttonType.getTextColor(),
    disabledBackgroundTint = AppColor.Java300,
    disabledTextColor = AppColor.Java20,
    viewState = viewState,
    rippleTheme = buttonType.getRippleTheme(),
    borderStroke = buttonType.getBorderStroke(enabled && viewState != ButtonViewState.LOADING)
)

@OptIn(ExperimentalMaterial3Api::class)
private val primaryButtonGroupRippleTheme = RippleConfiguration(
    rippleAlpha = RippleAlpha(
        pressedAlpha = 1f,
        focusedAlpha = 1f,
        draggedAlpha = 1f,
        hoveredAlpha = 1f
    ),
    color = AppColor.Java300,
)

@OptIn(ExperimentalMaterial3Api::class)
private val secondaryButtonGroupRippleTheme = RippleConfiguration(
    rippleAlpha = RippleAlpha(
        pressedAlpha = 1f,
        focusedAlpha = 1f,
        draggedAlpha = 1f,
        hoveredAlpha = 1f
    ),
    color = AppColor.Java300,
)

@OptIn(ExperimentalMaterial3Api::class)
private val outlineButtonGroupRippleTheme = RippleConfiguration(
    rippleAlpha = RippleAlpha(
        pressedAlpha = 1f,
        focusedAlpha = 1f,
        draggedAlpha = 1f,
        hoveredAlpha = 1f
    ),
    color = AppColor.Java400,
)