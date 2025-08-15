package akhoi.libs.mlct.tools

import androidx.annotation.VisibleForTesting
import kotlin.experimental.or
import kotlin.math.min

class ByteConcat(initCap: Int) {
    private var bucket = ByteArray(initCap)

    @VisibleForTesting
    var position = 0
    private set

    fun getContent(): ByteArray = bucket.copyOf((position + 7) shr 3)

    fun appendInt(value: Int, size: Int) {
        if (size ushr 31 == 1) {
            return
        }
        var remaining = min(size, 32)
        var byte: Int
        var available: Int
        while (remaining > 0) {
            byte = position shr 3
            if (byte xor bucket.size == 0) {
                expandBucket()
            }
            available = 8 - (position and 7)
            bucket[byte] = bucket[byte] or
                    (value shl (32 - remaining) ushr (32 - available)).toByte()
            position += min(remaining, available)
            remaining -= available
        }
    }

    private fun expandBucket() {
        val capableBucket = ByteArray(bucket.size * 2)
        bucket.copyInto(capableBucket)
        bucket = capableBucket
    }

    fun appendLong(value: Long, size: Int) {
        if (size <= 32) {
            appendInt(value.toInt(), size)
        } else {
            val maskedValue = value and (-1L shl size).inv()
            appendInt((maskedValue shr 32).toInt(), size - 32)
            appendInt(maskedValue.toInt(), 32)
        }
    }

    fun align() {
        position = (position + 7) and -8
    }
}