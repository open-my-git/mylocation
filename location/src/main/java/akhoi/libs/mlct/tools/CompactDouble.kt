package akhoi.libs.mlct.tools

import kotlin.math.max
import kotlin.math.min

fun compressDouble(number: Double, desExpSize: Int, desSigSize: Int): Long = resizeDouble(
    java.lang.Double.doubleToRawLongBits(number),
    desExpSize = desExpSize,
    desSigSize = desSigSize
)

fun expandDouble(source: Long, srcExpSize: Int, srcSigSize: Int): Double = resizeDouble(
    source,
    srcExpSize = srcExpSize,
    srcSigSize = srcSigSize
).let(java.lang.Double::longBitsToDouble)

fun resizeDouble(
    src: Long,
    srcExpSize: Int = 11,
    srcSigSize: Int = 52,
    desExpSize: Int = 11,
    desSigSize: Int = 52
): Long {
    val srcActualExpSize = min(max(srcExpSize, 0), 11)
    val srcActualSigSize = min(max(srcSigSize, 1), 52)
    val desActualExpSize = min(max(desExpSize, 0), 11)
    val desActualSigSize = min(max(desSigSize, 1), 52)
    val desExpMask = (1L shl desActualExpSize) - 1
    val desBias = desExpMask shr 1
    val desExponent = when {
        desActualExpSize == 0 -> 0
        srcActualExpSize == 0 -> 1 + desBias and desExpMask
        else -> {
            val srcExpMask = (1L shl srcActualExpSize) - 1
            val srcBias = srcExpMask shr 1
            val srcExponent = src shr srcActualSigSize and srcExpMask
            when (srcExponent) {
                0L -> 0L
                srcExpMask -> desExpMask
                else -> srcExponent - srcBias + desBias and desExpMask
            }
        }
    }

    val srcSignificandMask = (1L shl srcActualSigSize) - 1
    val desSignificandMask = (1L shl desActualSigSize) - 1
    val desSignificand = src and srcSignificandMask shl max(desActualSigSize - srcActualSigSize, 0) shr max(srcActualSigSize - desActualSigSize, 0) and desSignificandMask
    val desSign = src shr (srcActualExpSize + srcActualSigSize) and 1L
    return desSign shl desActualExpSize or desExponent shl desActualSigSize or desSignificand
}
