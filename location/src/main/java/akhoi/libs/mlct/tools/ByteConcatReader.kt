package akhoi.libs.mlct.tools

import kotlin.math.min

class ByteConcatReader(private val content: ByteArray) {
    private var position: Int = 0

    fun readInt(size: Int): Int {
        if (size <= 0) {
            return 0
        }
        var result = 0
        
        var byte: Int
        var available: Int
        var remaining = min(size, 32)
        val totalBytes = content.size * 8
        while (remaining > 0 && position < totalBytes) {
            byte = position shr 3
            available = 8 - (position and 7)
            val extractBits = min(remaining, available)
            val extracted = ((1 shl extractBits) - 1) and (content[byte].toInt() ushr (available - extractBits))
            result = (result shl extractBits) or extracted
            position += extractBits
            remaining -= extractBits
        }
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
}