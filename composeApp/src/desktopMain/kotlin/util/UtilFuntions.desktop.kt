package util

import java.time.Instant

actual fun getCurrentTimeStamp(): String {
   return Instant.now().toEpochMilli().toString()
}