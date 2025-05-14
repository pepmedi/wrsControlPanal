package util

import PrimaryAppColor
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import com.dokar.sonner.ToastType
import com.dokar.sonner.Toaster
import com.dokar.sonner.rememberToasterState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.pdfbox.rendering.PDFRenderer

@Composable
fun PdfViewerWithLoading(
    url: String,
    onCancelClick: () -> Unit
) {
    val toaster = rememberToasterState()
    var toastEvent by remember { mutableStateOf<ToastEvent?>(null) }

    LaunchedEffect(toastEvent?.id) {
        toastEvent?.let {
            toaster.show(
                message = it.message,
                type = it.type
            )
        }
    }

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
                .padding(16.dp),
            downloadMessage = {
                toastEvent = it
            }
        )
        Toaster(
            state = toaster,
            richColors = true,
            alignment = Alignment.TopEnd
        )
    }
}

@Composable
fun PdfDownloadButton(
    url: String,
    modifier: Modifier = Modifier,
    downloadMessage: (ToastEvent) -> Unit
) {
    var isDownloading by remember { mutableStateOf(false) }

    // Use LaunchedEffect inside the Composable for async download handling
    val scope = rememberCoroutineScope()

    Button(
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryAppColor),
        onClick = {
            // Handle the button click
            isDownloading = true
            downloadMessage(ToastEvent(message = "Downloading...", type = ToastType.Info))

            // Launch the download task in a coroutine scope
            scope.launch {
                try {
                    // Set the destination path (you can change this to your desired location)
                    val destinationPath =
                        "${System.getProperty("user.home")}/Downloads/downloaded_pdf.pdf"
                    downloadPdf(url, destinationPath)
                    downloadMessage(
                        ToastEvent(
                            message = "Download complete!",
                            type = ToastType.Success
                        )
                    )
                } catch (e: Exception) {
                    downloadMessage(
                        ToastEvent(
                            message = "Download failed: \n ${e.message}",
                            type = ToastType.Error
                        )
                    )
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
}