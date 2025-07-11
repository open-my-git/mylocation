package akhoi.libs.tools

import kotlin.experimental.or
import kotlin.math.min

class TruncateConcatByteArray(capacity: Int) {
    val content = ByteArray(capacity)
    private var bitIndex = 0

    fun appendLong(value: Long, valueSize: Int) {
        var byteIndex = bitIndex shr 3
        var unalignedBits = 8 - (bitIndex and 7)

        var remainingSize = min(valueSize, 64)
        while (remainingSize > 0 && byteIndex < content.size) {
            content[byteIndex] = content[byteIndex] or
                    (value shl (64 - remainingSize) ushr (64 - unalignedBits)).toByte()
            ++byteIndex
            bitIndex += min(remainingSize, unalignedBits)
            remainingSize -= unalignedBits
            unalignedBits = 8
        }
    }

    fun appendDouble(value: Double, valueSize: Int) = appendLong(
        java.lang.Double.doubleToRawLongBits(value),
        valueSize
    )
}