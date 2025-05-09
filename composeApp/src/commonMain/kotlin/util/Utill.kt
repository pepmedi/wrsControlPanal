package util

object Util {
    fun String.toEncodeURLPath(): String =
        java.net.URLEncoder.encode(this, "UTF-8").replace("+", "%20")

    fun String.toNameFormat(): String {
        return this.split(" ").joinToString(" ") { it ->
            it.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                ) else it.toString()
            }
        }
    }

    fun String.toMobileFormat(): String {
        return if (this.startsWith("+91")) this else "+91$this"
    }

    fun String.toOtpMobileFormat(): String {
        return "91$this"
    }

    fun generateUUID(): String {
        return listOf(
            (1..8),
            (1..4),
            (1..4),
            (1..4),
            (1..12)
        ).joinToString("-") { section ->
            section.map {
                "0123456789abcdef".random()
            }.joinToString("")
        }
    }

    fun String.encodeFirebasePath(): String {
        return this.replace("/", "%2F")
    }
}