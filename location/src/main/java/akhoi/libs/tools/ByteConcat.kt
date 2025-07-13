package akhoi.libs.tools

import androidx.annotation.VisibleForTesting
import kotlin.experimental.or
import kotlin.math.min

class ByteConcat(capacity: Int) {
    val content = ByteArray(capacity)

    @VisibleForTesting
    var position = 0
    private set

    fun appendInt(value: Int, size: Int) {
        if (size <= 0) {
            return
        }
        var remaining = min(size, 32)
        var byte: Int
        var available: Int
        val contentBytes = content.size * 8
        while (remaining > 0 && position < contentBytes) {
            byte = position shr 3
            available = 8 - (position and 7)
            content[byte] = content[byte] or
                    (value shl (32 - remaining) ushr (32 - available)).toByte()
            position += min(remaining, available)
            remaining -= available
        }
    }
}