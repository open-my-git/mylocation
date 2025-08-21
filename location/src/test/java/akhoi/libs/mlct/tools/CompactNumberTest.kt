package akhoi.libs.mlct.tools

import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CompactNumberTest {
    @Test
    fun testCompactDouble_positive_normalSizes() {
        val actual = compactDouble(3.14, 4, 31)
        assertEquals(0x448F5C28F, actual)
    }

    @Test
    fun testCompactDouble_positive_sameSSize_eSizeOne() {
        val actual = compactDouble(56.18, 1, 20)
        assertEquals(0x1C170A, actual)
    }

    @Test
    fun testCompactDouble_positive_sameSSize_eSizeZero() {
        val actual = compactDouble(56.18, 0, 20)
        assertEquals(0xC170A, actual)
    }

    @Test
    fun testCompactDouble_positive_sameSSize_eSizeNegative() {
        val actual = compactDouble(56.18, -8, 20)
        assertEquals(0xC170A, actual)
    }

    @Test
    fun testCompactDouble_positive_sameESize_sSizeOne() {
        val actual = compactDouble(56.18, 5, 1)
        assertEquals(0x29, actual)
    }

    @Test
    fun testCompactDouble_positive_sameESize_sSizeZero() {
        val actual = compactDouble(56.18, 5, 0)
        assertEquals(0x29, actual)
    }

    @Test
    fun testCompactDouble_positive_sameESize_sSizeNegative() {
        val actual = compactDouble(56.18, 5, -3)
        assertEquals(0x29, actual)
    }

    @Test
    fun testCompactDouble_negative_normalSizes() {
        val actual = compactDouble(-3.14, 4, 31)
        assertEquals(0xC48F5C28F, actual)
    }

    @Test
    fun testCompactDouble_negative_sameSSize_eSizeOne() {
        val actual = compactDouble(-56.18, 1, 20)
        assertEquals(0x3C170A, actual)
    }

    @Test
    fun testCompactDouble_negative_sameSSize_eSizeZero() {
        val actual = compactDouble(-56.18, 0, 20)
        assertEquals(0x1C170A, actual)
    }

    @Test
    fun testCompactDouble_negative_sameSSize_eSizeNegative() {
        val actual = compactDouble(-56.18, -8, 20)
        assertEquals(0x1C170A, actual)
    }

    @Test
    fun testCompactDouble_negative_sameESize_sSizeOne() {
        val actual = compactDouble(-56.18, 5, 1)
        assertEquals(0x69, actual)
    }

    @Test
    fun testCompactDouble_negative_sameESize_sSizeZero() {
        val actual = compactDouble(-56.18, 5, 0)
        assertEquals(0x69, actual)
    }

    @Test
    fun testCompactDouble_negative_sameESize_sSizeNegative() {
        val actual = compactDouble(-56.18, 5, -3)
        assertEquals(0x69, actual)
    }

    @Test
    fun testCompactDouble_NaN() {
        assertFailsWith<IllegalArgumentException> {
            compactDouble(Double.NaN, 5, -3)
        }
    }

    @Test
    fun testCompactDouble_positive_subnormal() {
        val actual = compactDouble(1.1508711201542864e-308, 5, 3)
        assertEquals(0x4, actual)
    }

    @Test
    fun testCompactDouble_negative_subnormal() {
        val actual = compactDouble(-1.1508711201542864e-308, 5, 3)
        assertEquals(0x104, actual)
    }

    @Test
    fun testCompactDouble_positive_infinity() {
        val actual = compactDouble(Double.POSITIVE_INFINITY, 3, 7)
        assertEquals(0x380, actual)
    }

    @Test
    fun testCompactDouble_negative_infinity() {
        val actual = compactDouble(Double.NEGATIVE_INFINITY, 3, 7)
        assertEquals(0x780, actual)
    }

    @Test
    fun testCompactDouble_positive_zero() {
        val actual = compactDouble(0.0, 5, 2)
        assertEquals(0, actual)
    }

    @Test
    fun testCompactDouble_negative_zero() {
        val actual = compactDouble(-0.0, 5, 2)
        assertEquals(0x80, actual)
    }

    @Test
    fun testRestoreDouble_positive_subDoubleSize() {
        val actual = restoreDouble(0x448F5C28F, 4, 31)
        assertEquals(3.14, (actual * 100).roundToInt() / 100.0)
    }

    @Test
    fun testRestoreDouble_positive_doubleSize() {
        val actual = restoreDouble(0x40091EB851EB851F, 11, 52)
        assertEquals(3.14, actual)
    }

    @Test
    fun testRestoreDouble_negative_subDoubleSize() {
        val actual = restoreDouble(0xC48F5C28F, 4, 31)
        assertEquals(-3.14, (actual * 100).roundToInt() / 100.0)
    }

    @Test
    fun testRestoreDouble_negative_doubleSize() {
        val actual = restoreDouble(-0x3FF6E147AE147AE1, 11, 52)
        assertEquals(-3.14, actual)
    }

    @Test
    fun testRestoreDouble_zeroExponentLen() {
        val actual = restoreDouble(0xF8F5C28F5C29, 0, 52)
        assertEquals(1.35242770462391E-309, actual)
    }
}
