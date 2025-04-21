package core

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import util.FileUtil
import java.io.File

@Composable
fun CancelButton(
    onBackClick: () -> Unit,
    textColor: Color = Color.White,
    modifier: Modifier = Modifier,
    text: String = "Cancel",
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(Color.Gray.copy(alpha = 0.3f), Color.Gray.copy(alpha = 0.3f))
    )
    Box(
        modifier = modifier.fillMaxWidth()
            .padding(top = 16.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(gradient)
            .clickable {
                onBackClick()
            }
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = textColor
        )
    }
}

@Composable
fun ImageSelector(
    imageBitmap: ImageBitmap?,
    imageUrl: String? = null,
    onImageSelected: (File) -> Unit,
    errorMessage: (String) -> Unit,
    text: String = "Click to Select"
) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .background(Color.White)
            .border(2.dp, color = Color.Black)
            .clickable {
                val file = FileUtil.selectImage()
                file?.let {
                    // Optional: validate file extension (jpg/png/webp)
                    if (it.extension.lowercase() in listOf("jpg", "jpeg", "png", "webp")) {
                        onImageSelected(it)
                    } else {
                        errorMessage("Invalid file type! Please select a JPEG, PNG, or WebP image.")
                    }
                } ?: errorMessage("No file selected or file not accessible.")
            },
        contentAlignment = Alignment.Center
    ) {
        when {
            imageBitmap != null -> {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = "Selected Image",
                    modifier = Modifier.fillMaxSize()
                )
            }

            !imageUrl.isNullOrEmpty() -> {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Image from URL",
                    modifier = Modifier.fillMaxSize()
                )
            }

            else -> {
                Text(text = text)
            }
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