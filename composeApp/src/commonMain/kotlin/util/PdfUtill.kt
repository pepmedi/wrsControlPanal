package util

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.PDFRenderer
import org.jetbrains.skiko.toBitmap
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.URI
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import javax.imageio.ImageIO

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
fun convertPdfPageToImage(renderer: PDFRenderer, pageIndex: Int): Painter? {
    return try {
        val image = renderer.renderImage(pageIndex, 2f)
        val bitmap = image.toBitmap()
        BitmapPainter(bitmap.asComposeImageBitmap())
    } catch (e: Exception) {
        println("Error rendering page $pageIndex: ${e.message}")
        null
    }
}

@Composable
fun PdfViewerWithLoading(url: String) {
    var pdfPainters by remember { mutableStateOf<List<Painter>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(url) {
        isLoading = true
        loadError = null

        try {
            val document = withContext(Dispatchers.IO) { fetchPdfFromUrl(url) }
            val renderer = PDFRenderer(document)
            val pageCount = document.numberOfPages

            val painters = mutableListOf<Painter>()

            for (pageIndex in 0 until pageCount) {
                try {
                    val painter = withContext(Dispatchers.Default) {
                        convertPdfPageToImage(renderer, pageIndex)
                    }
                    painter?.let { painters.add(it) }
                } catch (e: Exception) {
                    println("Failed to render page $pageIndex: ${e.message}")
                }
            }

            document.close()
            delay(300) // Optional UI smoothness
            pdfPainters = painters

        } catch (e: Exception) {
            loadError = e.message
            println("Error loading PDF: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            loadError != null -> Text(
                text = "Failed to load PDF:\n$loadError",
                modifier = Modifier.align(Alignment.Center),
                color = androidx.compose.ui.graphics.Color.Red
            )

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(pdfPainters) { painter ->
                    Image(
                        painter = painter,
                        contentDescription = "PDF Page",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                    )
                }
            }
        }

        // Button for downloading the PDF
        DownloadButton(
            url = url, modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }
}

@Composable
fun DownloadButton(url: String, modifier: Modifier = Modifier) {
    var isDownloading by remember { mutableStateOf(false) }
    var downloadMessage by remember { mutableStateOf("") }

    // Use LaunchedEffect inside the Composable for async download handling
    val scope = rememberCoroutineScope()

    Button(
        onClick = {
            // Handle the button click
            isDownloading = true
            downloadMessage = "Downloading..."

            // Launch the download task in a coroutine scope
            scope.launch {
                try {
                    // Set the destination path (you can change this to your desired location)
                    val destinationPath =
                        "${System.getProperty("user.home")}/Downloads/downloaded_pdf.pdf"
                    downloadPdf(url, destinationPath)
                    downloadMessage = "Download complete!"
                } catch (e: Exception) {
                    downloadMessage = "Download failed: ${e.message}"
                } finally {
                    isDownloading = false
                }
            }
        },
        modifier = modifier,
        enabled = !isDownloading
    ) {
        if (isDownloading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        } else {
            Text("Download PDF")
        }
    }

    if (downloadMessage.isNotEmpty()) {
        Text(
            text = downloadMessage,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
