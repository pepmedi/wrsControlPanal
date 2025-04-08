package core

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import util.FileUtil
import java.io.File

@Composable
fun CancelButton(onBackClick: () -> Unit,modifier: Modifier = Modifier) {
    Button(
        onClick = { onBackClick() },
        shape = RoundedCornerShape(5.dp),
        modifier = modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            contentColor = Color.Black,
            containerColor = Color.Gray
        )
    ) {
        Text("Cancel")
    }
}

@Composable
fun ImageSelector(
    imageBitmap: ImageBitmap?,
    onImageSelected: (File) -> Unit,
    snackBarMessage: (String) -> Unit,
) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .background(Color.White)
            .border(2.dp, color = Color.Black)
            .clickable {
                val file = FileUtil.selectImage()
                file?.let { onImageSelected(it) }
                if (file != null) {
                    onImageSelected(file)
                } else {
                    snackBarMessage("Invalid file type! Please select a JPEG, PNG, or WebP image.")
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (imageBitmap != null) {
            Image(bitmap = imageBitmap, contentDescription = "Selected Image")
        } else {
            Text(text = "Click to Select")
        }
    }
}

@Composable
fun ErrorSnackBar(errorMessage: String, modifier: Modifier, onDismiss: () -> Unit) {
    Snackbar(
        action = {
            TextButton(onClick = onDismiss) {
                Text("OKAY")
            }
        },
        modifier = modifier
    ) {
        Text(text = errorMessage, color = Color.White)
    }
}