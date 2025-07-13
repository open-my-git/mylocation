package akhoi.libs.tools

import kotlin.math.min

class ByteConcatReader(private val content: ByteArray) {
    var position: Int = 0

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

    fun reset() {
        position = 0
    }
}