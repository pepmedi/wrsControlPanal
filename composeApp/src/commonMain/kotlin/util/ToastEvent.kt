package util

import java.util.UUID

data class ToastEvent(val message: String = "", val id: String = UUID.randomUUID().toString())
