package akhoi.libs.mlct.tools

import kotlin.math.max
import kotlin.math.min

class ByteConcatReader(private val content: ByteArray) {
    private var position: Long = 0L

    fun readInt(size: Int): Int {
        var remaining = max(min(size, 32), 0)
        if (remaining == 0) return 0

        var byte = (position shr 3).toInt()
        val remainder = 8 - (position and 7).toInt()
        var readSize = min(remaining, remainder)
        var result = readByte(0, byte, readSize, remainder)
        remaining -= readSize
        if (remaining <= 0) return result

        readSize = min(remaining, 8)
        result = readByte(result, ++byte, readSize, 8)
        remaining -= readSize
        if (remaining <= 0) return result

        readSize = min(remaining, 8)
        result = readByte(result, ++byte, readSize, 8)
        remaining -= readSize
        if (remaining <= 0) return result

        readSize = min(remaining, 8)
        result = readByte(result, ++byte, readSize, 8)
        remaining -= readSize
        if (remaining <= 0) return result

        readSize = min(remaining, 8)
        result = readByte(result, byte + 1, readSize, 8)

        return result
    }

    fun readLong(size: Int): Long {
        val value: Long
        if (size <= 32) {
            value = readInt(size).toLong() and (1L shl size) - 1
        } else {
            val msBitsCount = size - 32
            val msBits = readInt(msBitsCount).toLong() and (1L shl msBitsCount) - 1
            val lsBits = readInt(32).toLong() and 0xFFFFFFFF
            value = msBits shl 32 or lsBits
        }
        return value
    }

    fun readDouble(size: Int): Double {
        val longValue = readLong(size)
        return java.lang.Double.longBitsToDouble(longValue)
    }

    fun reset() {
        position = 0
    }

    fun skip(count: Int) {
        position += count
    }

    private inline fun readByte(value: Int, byteIndex: Int, size: Int, remainder: Int): Int {
        if (byteIndex >= content.size) return value

        val read = ((1 shl size) - 1) and (content[byteIndex].toInt() ushr (remainder - size))
        position += size
        return (value shl size) or read
    }
}