package akhoi.libs.tools

import kotlin.math.max

@Throws(IllegalArgumentException::class)
fun compactDouble(number: Double, eSize: Int, sSize: Int): Long {
    if (number.isNaN()) {
        throw IllegalArgumentException("NaN not supported.")
    }
    val actualESize = max(eSize, 0)
    val actualSSize = max(sSize, 1)
    val raw = java.lang.Double.doubleToRawLongBits(number)
    val compExp: Long
    if (actualESize == 0) {
        compExp = 0L
    } else {
        val exponent = (raw shr 52) and 0x7FF
        val expMask = (-1L shl actualESize).inv()
        compExp = when (exponent) {
            0L -> 0L
            0x7FFL -> exponent and expMask
            else -> ((exponent - 1023) and expMask) + (1 shl (actualESize - 1)) - 1
        }
    }

    val significand = raw shl 12 ushr (64 - actualSSize)
    val sign = raw ushr 63
    return (((sign shl actualESize) or compExp) shl actualSSize) or significand
}