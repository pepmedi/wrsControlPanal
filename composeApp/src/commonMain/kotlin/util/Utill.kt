package util

object Util {
    fun String.toEncodeURLPath(): String =
        java.net.URLEncoder.encode(this, "UTF-8").replace("+", "%20")
}