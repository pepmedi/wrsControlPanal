package util

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Image
import java.awt.Color
import java.awt.FileDialog
import java.awt.Frame
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FilenameFilter
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import javax.imageio.ImageWriter

object FileUtil {
    // Function to load an image file
    fun loadImage(file: File): ImageBitmap? {
        return try {
            val bufferedImage: BufferedImage = ImageIO.read(file)
            bufferedImage.toComposeImageBitmap()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    //     Function to select an image file using FileDialog
    fun selectImage(): File? {
        val fileDialog = FileDialog(Frame(), "Select an Image", FileDialog.LOAD)

        // Set default directory
        fileDialog.directory = System.getProperty("user.home") + File.separator + "Desktop"

        // Show only image files
        fileDialog.filenameFilter = FilenameFilter { _, name ->
            name.endsWith(".jpeg", ignoreCase = true) ||
                    name.endsWith(".jpg", ignoreCase = true) ||
                    name.endsWith(".png", ignoreCase = true) ||
                    name.endsWith(".webp", ignoreCase = true)
        }

        fileDialog.isVisible = true

        val selectedFileName = fileDialog.file ?: return null // Handle cancel case

        val selectedFile = File(fileDialog.directory, selectedFileName)

        // ✅ Ensure the file is a proper hierarchical URI
        return if (selectedFile.exists() && selectedFile.isFile) {
            selectedFile
        } else {
            println("Invalid file selected!")
            null
        }
    }

    suspend fun loadAndCompressImage(
        file: File,
        compressionThreshold: Long = 60 * 1024
    ): ImageBitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val originalImage: BufferedImage =
                    ImageIO.read(file)?.toRGB() ?: return@withContext null
                val compressedBytes = compressImage(originalImage, compressionThreshold)

                // Convert compressed byte array back to BufferedImage
                val compressedImage = ImageIO.read(ByteArrayInputStream(compressedBytes))

                compressedImage?.toComposeImageBitmap()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun compressImage(image: BufferedImage, compressionThreshold: Long): ByteArray {
        var outputBytes: ByteArray
        var quality = 1.0f
        val format = "jpeg" // Use "jpeg" for compression (PNG is lossless)

        do {
            ByteArrayOutputStream().use { outputStream ->
                val writer: ImageWriter = ImageIO.getImageWritersByFormatName(format).next()
                val param: ImageWriteParam = writer.defaultWriteParam

                if (param.canWriteCompressed()) {
                    param.compressionMode = ImageWriteParam.MODE_EXPLICIT
                    param.compressionQuality = quality
                }

                writer.output = ImageIO.createImageOutputStream(outputStream)
                writer.write(null, IIOImage(image, null, null), param)
                writer.dispose()

                outputBytes = outputStream.toByteArray()
                quality -= 0.1f
            }
        } while (outputBytes.size > compressionThreshold && quality > 0.05f)

        return outputBytes
    }

    // Convert BufferedImage to Compose ImageBitmap using Skia
    private fun BufferedImage.toComposeImageBitmap(): ImageBitmap {
        val outputStream = ByteArrayOutputStream()
        ImageIO.write(this, "png", outputStream) // Convert to PNG format
        val byteArray = outputStream.toByteArray()
        return Image.makeFromEncoded(byteArray).toComposeImageBitmap() // Convert using Skia
    }

    // Convert BufferedImage to RGB (fixes CMYK issue)
    private fun BufferedImage.toRGB(): BufferedImage {
        if (this.type == BufferedImage.TYPE_INT_RGB) return this // Already in RGB

        val rgbImage = BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_RGB)
        val g: Graphics2D = rgbImage.createGraphics()
        g.drawImage(this, 0, 0, Color.WHITE, null) // Draw onto an RGB canvas
        g.dispose()

        return rgbImage
    }

    fun selectFile(): File? {
        val fileDialog = FileDialog(Frame(), "Select an Image or PDF (*.jpg, *.png, *.pdf)", FileDialog.LOAD).apply {
            directory = System.getProperty("user.home") + File.separator + "Desktop"
            filenameFilter = FilenameFilter { _, name ->
                name.endsWith(".jpeg", ignoreCase = true) ||
                        name.endsWith(".jpg", ignoreCase = true) ||
                        name.endsWith(".png", ignoreCase = true) ||
                        name.endsWith(".webp", ignoreCase = true) ||
                        name.endsWith(".pdf", ignoreCase = true)
            }
            isVisible = true
        }

        val selectedFileName = fileDialog.file ?: return null // Handle cancel case
        val selectedDirectory = fileDialog.directory ?: return null
        val selectedFile = File(fileDialog.directory, selectedFileName)

        // ✅ Ensure the file is a proper hierarchical URI
        return if (selectedFile.exists() && selectedFile.isFile) {
            println("Selected file: ${selectedFile.absolutePath}")
            selectedFile
        } else {
            println("Invalid file selected!")
            null
        }
    }
}