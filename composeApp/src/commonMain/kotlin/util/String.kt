package util

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
}

fun buildFormattedBlog(description: String): List<BlogElement> {
    val elements = mutableListOf<BlogElement>()
    val lines = description.lines().map { it.trim() }

    val cleanedLines = lines.filterIndexed { index, line ->
        line.isNotEmpty() || (index > 0 && lines[index - 1].isNotBlank())
    }

    var paragraphBuilder = AnnotatedString.Builder()

    fun flushParagraph() {
        if (paragraphBuilder.toAnnotatedString().text.isNotBlank()) {
            elements.add(BlogElement.Text(paragraphBuilder.toAnnotatedString()))
            paragraphBuilder = AnnotatedString.Builder()
        }
    }

    for ((index, line) in cleanedLines.withIndex()) {
        if (line.startsWith("$")) continue

        if (line.startsWith("!")) {
            flushParagraph()
            elements.add(BlogElement.Divider)
            continue
        }

        when {
            line.isBlank() -> {
                paragraphBuilder.append("\n")
            }

            line.startsWith("#") -> {
                val heading = line.removePrefix("#").trim()
                with(paragraphBuilder) {
                    withStyle(ParagraphStyle(lineHeight = 20.sp)) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color.Black)) {
                            append(heading)
                        }
                        append("\n")
                    }
                }
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
                        appendFormattedText(remainingPart)
                    }
                } else {
                    paragraphBuilder.appendFormattedText(content)
                }
                paragraphBuilder.append("\n")
            }

            else -> {
                paragraphBuilder.appendFormattedText(line)
                paragraphBuilder.append("\n")
            }
        }

        if (index < cleanedLines.lastIndex && cleanedLines[index + 1].isBlank()) {
            paragraphBuilder.append("\n")
        }
    }

    flushParagraph()
    return elements
}

private fun AnnotatedString.Builder.appendFormattedText(text: String) {
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



