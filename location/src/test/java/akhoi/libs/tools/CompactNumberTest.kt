package akhoi.libs.tools

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CompactNumberTest {
    @Test
    fun testCompactNumber_positive_normalSizes() {
        val actual = compactDouble(3.14, 4, 31)
        assertEquals(18403934863, actual)
    }

    @Test
    fun testCompactNumber_positive_sameSSize_eSizeOne() {
        val actual = compactDouble(56.18, 1, 20)
        assertEquals(1840906, actual)
    }

    @Test
    fun testCompactNumber_positive_sameSSize_eSizeZero() {
        val actual = compactDouble(56.18, 0, 20)
        assertEquals(792330, actual)
    }

    @Test
    fun testCompactNumber_positive_sameSSize_eSizeNegative() {
        val actual = compactDouble(56.18, -8, 20)
        assertEquals(792330, actual)
    }

    @Test
    fun testCompactNumber_positive_sameESize_sSizeOne() {
        val actual = compactDouble(56.18, 5, 1)
        assertEquals(41, actual)
    }

    @Test
    fun testCompactNumber_positive_sameESize_sSizeZero() {
        val actual = compactDouble(56.18, 5, 0)
        assertEquals(41, actual)
    }

    @Test
    fun testCompactNumber_positive_sameESize_sSizeNegative() {
        val actual = compactDouble(56.18, 5, -3)
        assertEquals(41, actual)
    }

    @Test
    fun testCompactNumber_negative_normalSizes() {
        val actual = compactDouble(-3.14, 4, 31)
        assertEquals(52763673231, actual)
    }

    @Test
    fun testCompactNumber_negative_sameSSize_eSizeOne() {
        val actual = compactDouble(-56.18, 1, 20)
        assertEquals(3938058, actual)
    }

    @Test
    fun testCompactNumber_negative_sameSSize_eSizeZero() {
        val actual = compactDouble(-56.18, 0, 20)
        assertEquals(1840906, actual)
    }

    @Test
    fun testCompactNumber_negative_sameSSize_eSizeNegative() {
        val actual = compactDouble(-56.18, -8, 20)
        assertEquals(1840906, actual)
    }

    @Test
    fun testCompactNumber_negative_sameESize_sSizeOne() {
        val actual = compactDouble(-56.18, 5, 1)
        assertEquals(105, actual)
    }

    @Test
    fun testCompactNumber_negative_sameESize_sSizeZero() {
        val actual = compactDouble(-56.18, 5, 0)
        assertEquals(105, actual)
    }

    @Test
    fun testCompactNumber_negative_sameESize_sSizeNegative() {
        val actual = compactDouble(-56.18, 5, -3)
        assertEquals(105, actual)
    }

    @Test
    fun testCompactNumber_NaN() {
        assertFailsWith<IllegalArgumentException> {
            compactDouble(Double.NaN, 5, -3)
        }
    }

    @Test
    fun testCompactNumber_positive_subnormal() {
        val actual = compactDouble(1.1508711201542864e-308, 5, 3)
        assertEquals(4, actual)
    }

    @Test
    fun testCompactNumber_negative_subnormal() {
        val actual = compactDouble(-1.1508711201542864e-308, 5, 3)
        assertEquals(260, actual)
    }

    @Test
    fun testCompactNumber_positive_infinity() {
        val actual = compactDouble(Double.POSITIVE_INFINITY, 3, 7)
        assertEquals(896, actual)
    }

    @Test
    fun testCompactNumber_negative_infinity() {
        val actual = compactDouble(Double.NEGATIVE_INFINITY, 3, 7)
        assertEquals(1920, actual)
    }

    @Test
    fun testCompactNumber_positive_zero() {
        val actual = compactDouble(0.0, 5, 2)
        assertEquals(0, actual)
    }

    @Test
    fun testCompactNumber_negative_zero() {
        val actual = compactDouble(-0.0, 5, 2)
        assertEquals(128, actual)
    }
}
