package util

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

@Composable
fun PdfViewerWithLoading(
    url: String,
    onCancelClick: () -> Unit
) {
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
            delay(300)
            pdfPainters = painters

        } catch (e: Exception) {
            loadError = e.message
            println("Error loading PDF: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        when {
            isLoading -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.White
            )

            loadError != null -> Text(
                text = "Failed to load PDF:\n$loadError",
                modifier = Modifier.align(Alignment.Center),
                color = Color.Red
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

        // Transparent top bar with cancel icon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart)
                .background(Color.LightGray.copy(alpha = 0.5f)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCancelClick) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cancel",
                    tint = Color.White
                )
            }
        }

        // Bottom-right download button
        PdfDownloadButton(
            url = url,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }
}

@Composable
fun AsyncImageViewer(
    url: String,
    onCancelClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        SubcomposeAsyncImage(
            model = url,
            contentDescription = "Medical Image",
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            loading = {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
            },
            error = {
                Text(
                    text = "Failed to load image.",
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        )

        // Transparent top bar with Cancel icon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 8.dp)
                .align(Alignment.TopStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCancelClick) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cancel",
                    tint = Color.White
                )
            }
        }

        // Bottom download button
        PdfDownloadButton(
            url = url,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }
}


@Composable
fun PdfDownloadButton(url: String, modifier: Modifier = Modifier) {
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
