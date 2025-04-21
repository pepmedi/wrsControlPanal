package doctor.screen.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun TextInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector? = null,
    isPassword: Boolean = false,
    enabled: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onClick: () -> Unit = {}
) {
    var isFocused by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        enabled = enabled,
        maxLines = maxLines,
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = Color.White,

            focusedContainerColor = Color.White,
            disabledTextColor = Color.Black,
            disabledContainerColor = Color.Transparent,
            disabledLabelColor = Color.Gray,
            disabledIndicatorColor = Color.Gray,
            disabledLeadingIconColor = Color.Gray,
            disabledPlaceholderColor = Color.Gray
        ),
        prefix = {
            if (icon != null) {
                Icon(icon, contentDescription = label)
            }
        },
        minLines = minLines,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Text),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .onFocusChanged { isFocused = it.isFocused }
            .clickable { onClick() }
    )
}