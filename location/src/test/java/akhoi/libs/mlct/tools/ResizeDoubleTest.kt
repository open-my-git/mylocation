package akhoi.libs.mlct.tools

import kotlin.test.Test
import kotlin.test.assertEquals

class ResizeDoubleTest {
    @Test
    fun testCompressDouble_positive() {
        val actual = compressDouble(3.14, 4, 31)
        assertEquals(0x448F5C28F, actual)
    }

    @Test
    fun testCompressDouble_positive_expSizeOne() {
        val actual = compressDouble(56.18, 1, 20)
        assertEquals(0x1C170A, actual)
    }

    @Test
    fun testCompressDouble_positive_expSizeZero() {
        val actual = compressDouble(56.18, 0, 20)
        assertEquals(0xC170A, actual)
    }

    @Test
    fun testCompressDouble_positive_negativeExpSize() {
        val actual = compressDouble(56.18, -8, 20)
        assertEquals(0xC170A, actual)
    }

    @Test
    fun testCompressDouble_positive_sigSizeOne() {
        val actual = compressDouble(56.18, 5, 1)
        assertEquals(0x29, actual)
    }

    @Test
    fun testCompressDouble_positive_sigSizeZero() {
        val actual = compressDouble(56.18, 5, 0)
        assertEquals(0x29, actual)
    }

    @Test
    fun testCompressDouble_positive_negativeSigSize() {
        val actual = compressDouble(56.18, 5, -3)
        assertEquals(0x29, actual)
    }

    @Test
    fun testCompressDouble_negative() {
        val actual = compressDouble(-3.14, 4, 31)
        assertEquals(0xC48F5C28F, actual)
    }

    @Test
    fun testCompressDouble_negative_expSizeOne() {
        val actual = compressDouble(-56.18, 1, 20)
        assertEquals(0x3C170A, actual)
    }

    @Test
    fun testCompressDouble_negative_zeroExpSize() {
        val actual = compressDouble(-56.18, 0, 20)
        assertEquals(0x1C170A, actual)
    }

    @Test
    fun testCompressDouble_negative_negativeExpSize() {
        val actual = compressDouble(-56.18, -8, 20)
        assertEquals(0x1C170A, actual)
    }

    @Test
    fun testCompressDouble_negative_sigSizeOne() {
        val actual = compressDouble(-56.18, 5, 1)
        assertEquals(0x69, actual)
    }

    @Test
    fun testCompressDouble_negative_sameESize_sSizeZero() {
        val actual = compressDouble(-56.18, 5, 0)
        assertEquals(0x69, actual)
    }

    @Test
    fun testCompressDouble_negative_sameESize_sSizeNegative() {
        val actual = compressDouble(-56.18, 5, -3)
        assertEquals(0x69, actual)
    }

    @Test
    fun testCompressDouble_positive_subnormal() {
        val actual = compressDouble(1.1508711201542864e-308, 5, 3)
        assertEquals(0x4, actual)
    }

    @Test
    fun testCompressDouble_negative_subnormal() {
        val actual = compressDouble(-1.1508711201542864e-308, 5, 3)
        assertEquals(0x104, actual)
    }

    @Test
    fun testCompressDouble_positiveInfinity() {
        val actual = compressDouble(Double.POSITIVE_INFINITY, 3, 7)
        assertEquals(0x380, actual)
    }

    @Test
    fun testCompressDouble_negativeInfinity() {
        val actual = compressDouble(Double.NEGATIVE_INFINITY, 3, 7)
        assertEquals(0x780, actual)
    }

    @Test
    fun testCompressDouble_positiveZero() {
        val actual = compressDouble(0.0, 5, 2)
        assertEquals(0, actual)
    }

    @Test
    fun testCompressDouble_negativeZero() {
        val actual = compressDouble(-0.0, 5, 2)
        assertEquals(0x80, actual)
    }

    @Test
    fun testExpandDouble_positive_partialDoubleSize() {
        val actual = expandDouble(0x448F5C28F, 4, 31)
        assertEquals(3.139999999664724, actual)
    }

    @Test
    fun testExpandDouble_positive_fullDoubleSize() {
        val actual = expandDouble(0x40091EB851EB851F, 11, 52)
        assertEquals(3.14, actual)
    }

    @Test
    fun testExpandDouble_negative_partialDoubleSize() {
        val actual = expandDouble(0xC48F5C28F, 4, 31)
        assertEquals(-3.139999999664724, actual)
    }

    @Test
    fun testExpandDouble_negative_fullDoubleSize() {
        val actual = expandDouble(-0x3FF6E147AE147AE1, 11, 52)
        assertEquals(-3.14, actual)
    }

    @Test
    fun testExpandDouble_zeroExponentSize() {
        val actual = expandDouble(0xF8F5C28F5C29, 0, 52)
        assertEquals(java.lang.Double.longBitsToDouble(0x4000F8F5C28F5C29), actual)
    }

    @Test
    fun testResizeDouble_subnormal() {
        val actual = resizeDouble(0x8C80029318421, desExpSize = 3, desSigSize = 52)
        assertEquals(0x8C80029318421, actual)
    }

    @Test
    fun testResizeDouble_NaN() {
        val actual = resizeDouble(0x7FF0000000000001, desExpSize = 5, desSigSize = 10)
        assertEquals(0x7C00, actual)
    }

    @Test
    fun testResizeDouble_havingUnwantedLeadingBits() {
        val actual = resizeDouble(0xA0C0B4448F5C28F, srcExpSize = 4, srcSigSize = 31)
        assertEquals(0x40091EB851E00000, actual)
    }

    @Test
    fun testResizeDouble_unconventionalSizes_compression() {
        val actual = resizeDouble(
            0xA0C0B4448F5C28F,
            srcExpSize = 4,
            srcSigSize = 27,
            desExpSize = 1,
            desSigSize = 13
        )
        assertEquals(0x3D7, actual)
    }

    @Test
    fun testResizeDouble_unconventionalSizes_expansion() {
        val actual = resizeDouble(
            0xA0C0B4448F5C28F,
            srcExpSize = 2,
            srcSigSize = 7,
            desExpSize = 10,
            desSigSize = 30
        )
        assertEquals(0x17FC7800000, actual)
    }

    @Test
    fun testResizeDouble_compressAndExpand() {
        val longVal = java.lang.Double.doubleToRawLongBits(179.1357)
        val compressed = resizeDouble(
            longVal,
            desExpSize = 4,
            desSigSize = 31
        )
        val expanded = resizeDouble(
            compressed,
            srcExpSize = 4,
            srcSigSize = 31
        )
        val doubleExpanded = java.lang.Double.longBitsToDouble(expanded)
        assertEquals(179.1356999874115, doubleExpanded)
    }
}
