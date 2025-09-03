package akhoi.libs.mlct.tools

import kotlin.experimental.or
import kotlin.math.max
import kotlin.math.min

class ByteConcat(initCap: Int = 2) {
    private var bucket = ByteArray(initCap)

    private var position = 0L

    fun getContent(): ByteArray = bucket.copyOf(((position + 7) shr 3).toInt())

    fun appendInt(value: Int, size: Int) {
        var remaining = max(min(size, 32), 0)
        var byte: Int
        var available = 8 - (position and 7).toInt()
        while (remaining > 0) synchronized(bucket) {
            byte = (position shr 3).toInt()
            if (byte == bucket.size) {
                expandBucket()
            }
            val truncated = (value shl (32 - remaining) ushr (32 - available)).toByte()
            bucket[byte] = bucket[byte] or truncated
            position += min(remaining, available)
            remaining -= available
            available = 8
        }
    }

    fun appendLong(value: Long, size: Int) {
        if (size <= 32) {
            appendInt(value.toInt(), size)
        } else {
            appendInt((value shr 32).toInt(), size - 32)
            appendInt(value.toInt(), 32)
        }
    }

    private fun expandBucket(expected: Int = bucket.size + 1) = synchronized(bucket) {
        val capableSize = bucket.size + max(expected - bucket.size, bucket.size shr 1)
        if (capableSize < bucket.size) {
            throw OutOfMemoryError("Expectation $expected above the valid range.")
        }
        bucket = bucket.copyOf(capableSize)
    }
}