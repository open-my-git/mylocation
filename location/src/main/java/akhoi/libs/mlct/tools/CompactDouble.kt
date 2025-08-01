package akhoi.libs.mlct.tools

import kotlin.math.max
import kotlin.math.min

@Throws(IllegalArgumentException::class)
fun compactDouble(number: Double, eSize: Int, sSize: Int): Long {
    if (number.isNaN()) {
        throw IllegalArgumentException("NaN not supported.")
    }
    val actualESize = max(eSize, 0)
    val actualSSize = max(sSize, 1)
    val raw = java.lang.Double.doubleToRawLongBits(number)
    val exponent: Long
    if (actualESize == 0) {
        exponent = 0L
    } else {
        val rawExpMask = 0x7FFL
        val rawExp = (raw shr 52) and rawExpMask
        val expMask = (-1L shl actualESize).inv()
        val bias = (1L shl (actualESize - 1)) - 1
        exponent = when (rawExp) {
            0L -> 0L
            rawExpMask -> expMask
            else -> ((rawExp - 1023) and expMask) + bias
        }
    }

    val significand = raw shl 12 ushr (64 - actualSSize)
    val sign = raw ushr 63
    return sign shl actualESize or exponent shl actualSSize or significand
}

fun restoreDouble(source: Long, expLen: Int, significandLen: Int): Double {
    val actualExpLen = min(max(0, expLen), 11)
    val actualSfLen = min(max(0, significandLen), 52)
    val resExponent: Long
    if (actualExpLen == 0) {
        resExponent = 0
    } else {
        val expMask = (-1L shl actualExpLen).inv()
        val exponent = source shr actualSfLen and expMask
        val bias = (1L shl (actualExpLen - 1)) - 1
        resExponent = when (exponent) {
            0L -> 0L
            expMask -> 0x7FF
            else -> exponent - bias + 1023
        }
    }
    val significand = source and (-1L shl actualSfLen).inv()
    val sign = source shr (actualExpLen + actualSfLen)
    val raw = sign shl 11 or resExponent shl 52 or (significand shl 52 - actualSfLen)
    return java.lang.Double.longBitsToDouble(raw)
}
