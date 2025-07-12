package akhoi.libs.tools

import androidx.annotation.VisibleForTesting
import kotlin.experimental.or
import kotlin.math.min

class TruncateConcatByteArray(capacity: Int) {
    val content = ByteArray(capacity)

    @VisibleForTesting
    var position = 0
    private set

    fun appendInt(value: Int, size: Int) {
        var byte = position shr 3
        var unaligned = 8 - (position and 7)

        var remaining = min(size, 32)
        while (remaining > 0 && byte < content.size) {
            content[byte] = content[byte] or
                    (value shl (32 - remaining) ushr (32 - unaligned)).toByte()
            position += min(remaining, unaligned)
            remaining -= unaligned
            unaligned = 8
            ++byte
        }
    }
}