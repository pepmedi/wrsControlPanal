package util

import com.dokar.sonner.ToastType
import java.util.UUID

data class ToastEvent(
    val message: String = "",
    val id: String = UUID.randomUUID().toString(),
    val type: ToastType = ToastType.Error
)
