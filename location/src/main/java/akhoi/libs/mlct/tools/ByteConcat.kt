package akhoi.libs.mlct.tools

import kotlin.experimental.or
import kotlin.math.max
import kotlin.math.min

class ByteConcat(initCap: Int = 2) {
    private var bucket = ByteArray(initCap)

    private var position = 0L

    fun getContent(): ByteArray = bucket.copyOf(((position + 7) shr 3).toInt())

    fun appendInt(value: Int, size: Int) {
        if (size ushr 31 == 1) {
            return
        }
        var remaining = min(size, 32)
        var byte: Int
        var available: Int
        while (remaining > 0) {
            byte = (position shr 3).toInt()
            if (byte xor bucket.size == 0) {
                expandBucket()
            }
            available = (8 - (position and 7)).toInt()
            bucket[byte] = bucket[byte] or
                    (value shl (32 - remaining) ushr (32 - available)).toByte()
            position += min(remaining, available)
            remaining -= available
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

    private fun expandBucket(expected: Int = bucket.size + 1) {
        val capableSize = bucket.size + max(expected - bucket.size, bucket.size shr 1)
        if (capableSize < 0) {
            throw OutOfMemoryError("Expectation $expected above the valid range.")
        }
        bucket = bucket.copyOf(capableSize)
    }
}