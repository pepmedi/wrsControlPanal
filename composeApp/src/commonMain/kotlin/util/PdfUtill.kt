package util

import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.PDFRenderer
import org.jetbrains.skiko.toBitmap
import java.io.InputStream
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

// Function to fetch PDF from URL
suspend fun fetchPdfFromUrl(url: String): PDDocument = withContext(Dispatchers.IO) {
    val uri = URI(url)
    uri.toURL().openStream().use { PDDocument.load(it) }
}

// Function to download PDF
suspend fun downloadPdf(url: String, destinationPath: String) {
    withContext(Dispatchers.IO) {
        val inputStream: InputStream = URI(url).toURL().openStream()
        val path = Paths.get(destinationPath)
        Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING)
        inputStream.close()
    }
}

// Function to convert PDF page to image (no need to be @Composable)
fun convertPdfPageToImage(renderer: PDFRenderer, pageIndex: Int = 0): Painter? {
    return try {
        val image = renderer.renderImage(pageIndex, 2f)
        val bitmap = image.toBitmap()
        BitmapPainter(bitmap.asComposeImageBitmap())
    } catch (e: Exception) {
        println("Error rendering page $pageIndex: ${e.message}")
        null
    }
}
