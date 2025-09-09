package akhoi.libs.mlct.tools

import androidx.annotation.VisibleForTesting
import kotlin.experimental.or
import kotlin.math.max
import kotlin.math.min

class ByteConcat(initCap: Int = 2) {
    private var bucket = ByteArray(initCap.takeHighestOneBit())

    private var position = 0L

    // todo: prevent copy always
    fun getContent(): ByteArray = bucket.copyOf(((position + 7) shr 3).toInt())

    fun appendInt(value: Int, size: Int) {
        var actualSize = max(min(size, 32), 0)
        if (actualSize == 0) return

        var byte = (position shr 3).toInt()
        actualSize = appendHighestByte(value, actualSize, byte, 8 - (position and 7).toInt())
        actualSize = appendHighestByte(value, actualSize, ++byte, 8)
        actualSize = appendHighestByte(value, actualSize, ++byte, 8)
        actualSize = appendHighestByte(value, actualSize, ++byte, 8)
        appendHighestByte(value, actualSize, byte + 1, 8)
    }

    fun appendLong(value: Long, size: Int) {
        if (size <= 32) {
            appendInt(value.toInt(), size)
        } else {
            appendInt((value shr 32).toInt(), size - 32)
            appendInt(value.toInt(), 32)
        }
    }

    private inline fun appendHighestByte(value: Int, size: Int, bucketIndex: Int, bucketRemainder: Int): Int {
        if (size <= 0) return size

        synchronized(bucket) {
            if (bucketIndex == bucket.size) {
                val capableSize = max(bucket.size shl 1, bucket.size or (bucket.size shr 1))
                if (capableSize < bucket.size) {
                    throw OutOfMemoryError("Bucket size overflow, current: ${bucket.size}, next: $capableSize")
                }
                bucket = bucket.copyOf(capableSize)
            }
            bucket[bucketIndex] = bucket[bucketIndex] or (value shl (32 - size) ushr (32 - bucketRemainder)).toByte()
        }

        position += min(size, bucketRemainder)

        return size - bucketRemainder
    }

    @VisibleForTesting
    fun getBucketSize() = bucket.size
}