package util

import PrimaryAppColor
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.dokar.sonner.ToastType
import com.dokar.sonner.Toaster
import com.dokar.sonner.rememberToasterState
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.request.get
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext
import java.io.File

@Composable
fun AsyncImageViewer(
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
        ImageDownloadButton(
            url = url,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            downloadMessage = { toastEvent = it }
        )

        Toaster(
            state = toaster,
            richColors = true,
            alignment = Alignment.TopEnd
        )
    }
}

@Composable
fun ImageDownloadButton(
    url: String,
    fileName: String = "downloaded_image.jpg",
    modifier: Modifier = Modifier,
    downloadMessage: (ToastEvent) -> Unit
) {
    var isDownloading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Button(
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryAppColor),
        onClick = {
            isDownloading = true
            downloadMessage(ToastEvent(message = "Downloading...", type = ToastType.Info))

            scope.launch {
                try {
                    val destinationPath =
                        "${System.getProperty("user.home")}/Downloads/$fileName"

                    downloadImage(url, destinationPath)

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
            Text("Download Image")
        }
    }
}

suspend fun downloadImage(url: String, destinationPath: String) {
    val engine: HttpClientEngine = GlobalContext.get().get()
    val client = HttpClient(engine)
    val byteArray = client.get(url).body<ByteArray>()
    File(destinationPath).writeBytes(byteArray)
    client.close()
}