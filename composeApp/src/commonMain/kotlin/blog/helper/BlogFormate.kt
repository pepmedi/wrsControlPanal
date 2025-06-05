package blog.helper

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp

sealed class BlogElement {
    data class Text(val content: AnnotatedString) : BlogElement()
    data object Divider : BlogElement()
    data class Image(val url: String) : BlogElement()
}

fun buildFormattedBlog(
    description: String,
    imageList: List<String> = emptyList()
): List<BlogElement> {
    val elements = mutableListOf<BlogElement>()
    val lines = description.lines().map { it.trim() }

    // Filter out unnecessary empty lines and skip redundant blanks
    val cleanedLines = lines.filterIndexed { index, line ->
        line.isNotEmpty() || (index > 0 && lines[index - 1].isNotBlank())
    }

    var paragraphBuilder = AnnotatedString.Builder()
    var imageIndex = 0

    fun flushParagraph() {
        if (paragraphBuilder.toAnnotatedString().text.isNotBlank()) {
            elements.add(BlogElement.Text(paragraphBuilder.toAnnotatedString()))
            paragraphBuilder = AnnotatedString.Builder()
        }
    }

    var index = 0
    while (index < cleanedLines.size) {
        val line = cleanedLines[index]

        if (line.startsWith("$")) {
            // Ignore metadata lines
            index++
            continue
        }

        if (line.startsWith("<image>")) {
            flushParagraph()
            if (imageIndex < imageList.size) {
                elements.add(BlogElement.Image(imageList[imageIndex]))
                imageIndex++
            }
            index++
            continue
        }

        if (line.startsWith("!")) {
            flushParagraph()
            elements.add(BlogElement.Divider)
            index++
            continue
        }

        if (line.startsWith("#")) {
            flushParagraph()

            val heading = line.removePrefix("#").trim()
            val headingBuilder = AnnotatedString.Builder().apply {
                withStyle(ParagraphStyle(lineHeight = 20.sp)) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color.Black)) {
                        append(heading)
                    }
                }
            }
            elements.add(BlogElement.Text(headingBuilder.toAnnotatedString()))

            // Look ahead to add a direct content line if exists
            var nextIndex = index + 1
            while (nextIndex < cleanedLines.size && cleanedLines[nextIndex].startsWith("$")) {
                nextIndex++
            }

            if (nextIndex < cleanedLines.size) {
                val subLine = cleanedLines[nextIndex]
                if (!subLine.startsWith("!") && subLine.isNotBlank()) {
                    val subBuilder = AnnotatedString.Builder().apply {
                        appendFormattedBlogText(subLine)
                    }
                    elements.add(BlogElement.Text(subBuilder.toAnnotatedString()))
                    index = nextIndex + 1
                    continue
                }
            }

            index++
            continue
        }

        when {
            line.isBlank() -> {
                paragraphBuilder.append("\n")
            }

            line.startsWith("•") -> {
                val content = line.removePrefix("•").trim()
                val firstWordEndsAt = content.indexOf(':')
                paragraphBuilder.append("• ")
                if (firstWordEndsAt != -1) {
                    val titlePart = content.substring(0, firstWordEndsAt + 1)
                    val remainingPart = content.substring(firstWordEndsAt + 1).trim()

                    with(paragraphBuilder) {
                        withStyle(
                            SpanStyle(
                                fontWeight = FontWeight.Bold,
                                color = Color.Black.copy(alpha = 0.7f)
                            )
                        ) {
                            append("$titlePart ")
                        }
                        appendFormattedBlogText(remainingPart)
                    }
                } else {
                    paragraphBuilder.appendFormattedBlogText(content)
                }
                paragraphBuilder.append("\n")
            }

            else -> {
                paragraphBuilder.appendFormattedBlogText(line)
                paragraphBuilder.append("\n")
            }
        }

        index++
    }

    flushParagraph()
    return elements
}


private fun AnnotatedString.Builder.appendFormattedBlogText(text: String) {
    var remaining = text
    while (remaining.contains("**")) {
        val before = remaining.substringBefore("**")
        val middleAndAfter = remaining.substringAfter("**")

        if (!middleAndAfter.contains("**")) {
            withStyle(SpanStyle(fontWeight = FontWeight.Normal, color = Color.DarkGray)) {
                append(remaining)
            }
            return
        }

        val boldPart = middleAndAfter.substringBefore("**")
        val after = middleAndAfter.substringAfter("**")

        withStyle(SpanStyle(fontWeight = FontWeight.Normal, color = Color.DarkGray)) {
            append(before)
        }
        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color.DarkGray)) {
            append(boldPart)
        }

        remaining = after
    }

    if (remaining.isNotEmpty()) {
        withStyle(SpanStyle(fontWeight = FontWeight.Normal, color = Color.DarkGray)) {
            append(remaining)
        }
    }
}

