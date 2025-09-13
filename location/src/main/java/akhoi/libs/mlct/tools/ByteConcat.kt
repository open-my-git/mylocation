package akhoi.libs.mlct.tools

import androidx.annotation.VisibleForTesting
import kotlin.experimental.or
import kotlin.math.max
import kotlin.math.min

class ByteConcat(initCap: Int = 2) {
    private var bucket = ByteArray(initCap.takeHighestOneBit())

    // todo: prevent copy always
    fun getContent(): ByteArray = bucket.copyOf(byte + ((position + 7) shr 3))

    private var byte: Int = 0
    private var position: Int = 0

    fun appendInt(value: Int, size: Int) {
        var actualSize = max(min(size, 32), 0)
        if (actualSize == 0) return

        actualSize = appendHighestByte(value, actualSize, 8 - position)
        actualSize = appendHighestByte(value, actualSize, 8)
        actualSize = appendHighestByte(value, actualSize, 8)
        actualSize = appendHighestByte(value, actualSize, 8)
        appendHighestByte(value, actualSize, 8)
    }

    fun appendLong(value: Long, size: Int) {
        if (size <= 32) {
            appendInt((value and 0xFFFFFFFF).toInt(), size)
        } else {
            appendInt((value ushr 32).toInt(), size - 32)
            appendInt((value and 0xFFFFFFFF).toInt(), 32)
        }
    }

    private inline fun appendHighestByte(value: Int, size: Int, remainder: Int): Int {
        if (size <= 0) return size

        synchronized(bucket) {
            if (byte == bucket.size) {
                val capableSize = max(bucket.size shl 1, bucket.size or (bucket.size shr 1))
                if (capableSize < bucket.size) {
                    throw OutOfMemoryError("Bucket size overflow, current: ${bucket.size}, next: $capableSize")
                }
                bucket = bucket.copyOf(capableSize)
            }
            bucket[byte] = bucket[byte] or (value shl (32 - size) ushr (32 - remainder)).toByte()
            val nextBytePosition = position + min(size, remainder)
            byte += nextBytePosition shr 3
            position = nextBytePosition and 7
        }

        return size - remainder
    }

    @VisibleForTesting
    fun getBucketSize() = bucket.size
}