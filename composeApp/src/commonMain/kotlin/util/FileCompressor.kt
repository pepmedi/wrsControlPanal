package util

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import java.io.*
import javax.imageio.ImageWriteParam
import javax.imageio.ImageWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Color
import java.awt.Graphics2D
import javax.imageio.IIOImage

object FileCompressor {

    suspend fun loadAndCompressImage(file: File, compressionThreshold: Long = 60 * 1024): File? {
        return withContext(Dispatchers.IO) {
            try {
                val originalImage: BufferedImage = ImageIO.read(file) ?: return@withContext null
                val compressedFile = compressImage(originalImage, file, compressionThreshold)
                compressedFile
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun compressImage(image: BufferedImage, originalFile: File, compressionThreshold: Long): File {
        // Define a prefix and maximum allowed length for the file name.
        val prefix = "compressed_"
        val maxFileNameLength = 100  // Adjust this value based on your file system's limits

        // Construct the file name using the prefix.
        var compressedFileName = "$prefix${originalFile.name}"
        // If the generated file name is too long, truncate the original file name portion.
        if (compressedFileName.length > maxFileNameLength) {
            val truncatedName = originalFile.name.take(maxFileNameLength - prefix.length)
            compressedFileName = "$prefix$truncatedName"
        }

        // Create the compressed file using the safe (possibly truncated) file name.
        val compressedFile = File(originalFile.parent, compressedFileName)

        // Convert image to RGB to avoid "Bogus input colorspace" error.
        val rgbImage = image.toRGB()

        var quality = 1.0f
        val format = "jpeg"
        var shouldStop = false

        while (!shouldStop) {
            ByteArrayOutputStream().use { outputStream ->
                val writer: ImageWriter = ImageIO.getImageWritersByFormatName(format).next()
                val param: ImageWriteParam = writer.defaultWriteParam

                if (format == "jpeg") {
                    param.compressionMode = ImageWriteParam.MODE_EXPLICIT
                    param.compressionQuality = quality
                }

                writer.output = ImageIO.createImageOutputStream(outputStream)
                writer.write(null, IIOImage(rgbImage, null, null), param) // Use RGB image
                writer.dispose()

                val outputBytes = outputStream.toByteArray()
                quality -= 0.1f

                // Write to file
                compressedFile.outputStream().use { it.write(outputBytes) }

                shouldStop = compressedFile.length() <= compressionThreshold || quality <= 0.05f
            }
        }

        return compressedFile
    }


    // Convert BufferedImage to RGB (fixes CMYK issue)
    // Convert any BufferedImage to RGB
    private fun BufferedImage.toRGB(): BufferedImage {
        if (this.type == BufferedImage.TYPE_INT_RGB) return this // Already in RGB

        val rgbImage = BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_RGB)
        val g: Graphics2D = rgbImage.createGraphics()
        g.drawImage(this, 0, 0, Color.WHITE, null) // Convert to RGB
        g.dispose()

        return rgbImage
    }


}