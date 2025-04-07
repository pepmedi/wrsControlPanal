package util

import SecondaryAppColor
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun CustomOutlinedText(
    value: String,
    placeHolder: String,
    maxLine: Int = 1,
    minLine: Int = 1,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it) },
        placeholder = { Text(placeHolder, color = Color.Black) },
        modifier = modifier
            .fillMaxWidth(),
        singleLine = false,
        minLines = minLine,
        maxLines = maxLine,
        keyboardOptions = keyboardOptions,
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            cursorColor = SecondaryAppColor,
            focusedBorderColor = SecondaryAppColor.copy(alpha = 0.1f),
            unfocusedBorderColor = SecondaryAppColor.copy(alpha = 0.1f),
            focusedContainerColor = SecondaryAppColor.copy(alpha = 0.1f),
            unfocusedContainerColor = SecondaryAppColor.copy(alpha = 0.1f)
        )
    )
}