package core

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.PDFRenderer
import org.jetbrains.skia.Image
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
fun FileSelector(
    imageBitmap: ImageBitmap?,
    fileName: String? = null,
    onFileSelected: (file: File, mimeType: String) -> Unit,
    errorMessage: (String) -> Unit,
    text: String = "Click to Select Image or PDF"
) {
    var selectedFileName by remember { mutableStateOf(fileName ?: "") }
    var selectedImageBitmap by remember { mutableStateOf(imageBitmap) }

    Box(
        modifier = Modifier
            .size(160.dp)
            .background(Color.White)
            .border(2.dp, color = Color.Black)
            .clickable {
                val file = FileUtil.selectFile()
                file?.let {
                    val ext = it.extension.lowercase()

                    if (ext in listOf("jpg", "jpeg", "png", "webp", "pdf")) {
                        val (bitmap, error) = when (ext) {
                            in listOf("jpg", "jpeg", "png", "webp") -> {
                                loadImageBitmapFromFile(it) to null
                            }

                            "pdf" -> {
                                renderFirstPageOfPdf(it)
                            }

                            else -> null to "Unsupported file type"
                        }

                        // ❌ If there's an error, show it and stop — treat as no selection
                        if (error != null || bitmap == null) {
                            errorMessage(error ?: "Failed to load the file.")
                            return@clickable
                        }

                        // ✅ No error, proceed to set image + filename + notify callback
                        selectedFileName = it.name
                        selectedImageBitmap = bitmap

                        val mimeType = when (ext) {
                            "jpg", "jpeg" -> "image/jpeg"
                            "png" -> "image/png"
                            "webp" -> "image/webp"
                            "pdf" -> "application/pdf"
                            else -> "application/octet-stream"
                        }

                        onFileSelected(it, mimeType)
                    } else {
                        errorMessage("Invalid file type! Select JPG, PNG, WebP, or PDF.")
                    }
                } ?: errorMessage("No file selected.")
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (selectedImageBitmap != null) {
                Image(
                    bitmap = selectedImageBitmap!!,
                    contentDescription = "Selected File",
                    modifier = Modifier
                        .size(100.dp)
                        .padding(4.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "No File",
                    modifier = Modifier.size(64.dp)
                )
            }

            Text(
                text = selectedFileName.ifBlank { text },
                modifier = Modifier.padding(top = 4.dp),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Helper function to load the selected image into an ImageBitmap
fun loadImageBitmapFromFile(file: File): ImageBitmap? {
    return try {
        val bytes = file.readBytes()
        Image.makeFromEncoded(bytes).toComposeImageBitmap()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun renderFirstPageOfPdf(file: File): Pair<ImageBitmap?, String?> {
    return try {
        if (!isPdfFile(file)) {
            return null to "Not a valid PDF file"
        }

        PDDocument.load(file).use { document ->
            if (document.numberOfPages > 0) {
                val renderer = PDFRenderer(document)
                val bufferedImage = renderer.renderImageWithDPI(0, 150f)
                bufferedImage.toComposeImageBitmap() to null
            } else {
                null to "PDF has no pages"
            }
        }
    } catch (e: Exception) {
        null to "PDF rendering error: ${e.message}"
    }
}

fun isPdfFile(file: File): Boolean {
    return try {
        val header = file.inputStream().use { stream ->
            ByteArray(4).apply { stream.read(this) }
        }
        header.contentEquals(byteArrayOf(0x25, 0x50, 0x44, 0x46)) // %PDF
    } catch (e: Exception) {
        false
    }
}