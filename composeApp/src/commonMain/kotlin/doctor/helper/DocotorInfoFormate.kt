package doctor.helper

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp

sealed class DoctorEducationElement {
    data class Text(val content: AnnotatedString) : DoctorEducationElement()
    data object Divider : DoctorEducationElement()
}


fun buildDoctorEducationFormatted(description: String): List<DoctorEducationElement> {
    val elements = mutableListOf<DoctorEducationElement>()
    val lines = description.lines().map { it.trim() }

    val cleanedLines = lines.filterIndexed { index, line ->
        line.isNotEmpty() || (index > 0 && lines[index - 1].isNotBlank())
    }

    var paragraphBuilder = AnnotatedString.Builder()

    fun flushParagraph() {
        if (paragraphBuilder.toAnnotatedString().text.isNotBlank()) {
            elements.add(DoctorEducationElement.Text(paragraphBuilder.toAnnotatedString()))
            paragraphBuilder = AnnotatedString.Builder()
        }
    }

    var index = 0
    while (index < cleanedLines.size) {
        val line = cleanedLines[index]

        if (line.startsWith("$")) {
            // Skip lines starting with $
            index++
            continue
        }

        if (line.startsWith("!")) {
            // Flush any pending paragraph, then add divider
            flushParagraph()
            elements.add(DoctorEducationElement.Divider)
            index++
            continue
        }

        if (line.startsWith("#")) {
            // Flush previous paragraph before heading
            flushParagraph()

            val heading = line.removePrefix("#").trim()
            val headingBuilder = AnnotatedString.Builder().apply {
                withStyle(ParagraphStyle(lineHeight = 20.sp)) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color.Black)) {
                        append(heading)
                    }
                }
            }
            elements.add(DoctorEducationElement.Text(headingBuilder.toAnnotatedString()))

            // Look ahead to find next content line (skip $ lines)
            var nextIndex = index + 1
            while (nextIndex < cleanedLines.size && cleanedLines[nextIndex].startsWith("$")) {
                nextIndex++
            }

            if (nextIndex < cleanedLines.size) {
                val subLine = cleanedLines[nextIndex]
                if (!subLine.startsWith("!") && subLine.isNotBlank()) {
                    val subBuilder = AnnotatedString.Builder().apply {
                        appendFormattedDoctorInfoText(subLine)
                    }
                    elements.add(DoctorEducationElement.Text(subBuilder.toAnnotatedString()))
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
                        appendFormattedDoctorInfoText(remainingPart)
                    }
                } else {
                    paragraphBuilder.appendFormattedDoctorInfoText(content)
                }
                paragraphBuilder.append("\n")
            }

            else -> {
                paragraphBuilder.appendFormattedDoctorInfoText(line)
                paragraphBuilder.append("\n")
            }
        }

        index++
    }

    flushParagraph()
    return elements
}

private fun AnnotatedString.Builder.appendFormattedDoctorInfoText(text: String) {
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