package akhoi.libs.mlct.tools.bcc

import org.junit.Assert
import org.junit.Test
import java.lang.Double

class ResizeDoubleTest {
    @Test
    fun testCompressDouble() {
        val actual = ResizeDouble.compressDouble(3.14, 4, 31)
        Assert.assertEquals(0x448F5C28FL, actual)
    }

    @Test
    fun testCompressDouble_expSizeOne() {
        val actual = ResizeDouble.compressDouble(56.18, 1, 20)
        Assert.assertEquals(0x1C170AL, actual)
    }

    @Test
    fun testCompressDouble_expSizeZero() {
        val actual = ResizeDouble.compressDouble(56.18, 0, 20)
        Assert.assertEquals(0x0C170AL, actual)
    }

    @Test
    fun testCompressDouble_negativeExpSize() {
        val actual = ResizeDouble.compressDouble(56.18, -8, 20)
        Assert.assertEquals(0x0C170AL, actual)
    }

    @Test
    fun testCompressDouble_sigSizeOne() {
        val actual = ResizeDouble.compressDouble(56.18, 5, 1)
        Assert.assertEquals(0x29L, actual)
    }

    @Test
    fun testCompressDouble_sigSizeZero() {
        val actual = ResizeDouble.compressDouble(56.18, 5, 0)
        Assert.assertEquals(0x29L, actual)
    }

    @Test
    fun testCompressDouble_negativeSigSize() {
        val actual = ResizeDouble.compressDouble(56.18, 5, -3)
        Assert.assertEquals(0x29L, actual)
    }

    @Test
    fun testCompressDouble_positiveSubnormal() {
        val actual = ResizeDouble.compressDouble(1.1508711201542864e-308, 5, 3)
        Assert.assertEquals(0x4L, actual)
    }

    @Test
    fun testCompressDouble_negativeSubnormal() {
        val actual = ResizeDouble.compressDouble(-1.1508711201542864e-308, 5, 3)
        Assert.assertEquals(0x104L, actual)
    }

    @Test
    fun testCompressDouble_positiveInfinity() {
        val actual = ResizeDouble.compressDouble(Double.POSITIVE_INFINITY, 3, 7)
        Assert.assertEquals(0x380L, actual)
    }

    @Test
    fun testCompressDouble_negativeInfinity() {
        val actual = ResizeDouble.compressDouble(Double.NEGATIVE_INFINITY, 3, 7)
        Assert.assertEquals(0x780L, actual)
    }

    @Test
    fun testCompressDouble_positiveZero() {
        val actual = ResizeDouble.compressDouble(0.0, 5, 2)
        Assert.assertEquals(0L, actual)
    }

    @Test
    fun testCompressDouble_negativeZero() {
        val actual = ResizeDouble.compressDouble(-0.0, 5, 2)
        Assert.assertEquals(0x80L, actual)
    }

    @Test
    fun testExpandDouble_partialDoubleSize() {
        val actual = ResizeDouble.expandDouble(0x448F5C28FL, 4, 31)
        Assert.assertEquals(3.139999999664724, actual, 0.0)
    }

    @Test
    fun testExpandDouble_fullDoubleSize() {
        val actual = ResizeDouble.expandDouble(0x40091EB851EB851FL, 11, 52)
        Assert.assertEquals(3.14, actual, 0.0)
    }

    @Test
    fun testExpandDouble_zeroExpSize() {
        val actual = ResizeDouble.expandDouble(0x0F8F5C28F5C29L, 0, 52)
        Assert.assertEquals(Double.longBitsToDouble(0x4000F8F5C28F5C29L), actual, 0.0)
    }

    @Test
    fun testResizeDouble_subnormal() {
        val actual = ResizeDouble.resizeDouble(0x8C80029318421L, 11, 52, 3, 52)
        Assert.assertEquals(0x8C80029318421L, actual)
    }

    @Test
    fun testResizeDouble_NaN() {
        val actual = ResizeDouble.resizeDouble(0x7FF0000000000001L, 11, 52, 5, 10)
        Assert.assertEquals(0x7C00L, actual)
    }

    @Test
    fun testResizeDouble_havingUnwantedLeadingBits() {
        val actual = ResizeDouble.resizeDouble(0x0A0C0B4448F5C28FL, 4, 31, 11, 52)
        Assert.assertEquals(0x40091EB851E00000L, actual)
    }

    @Test
    fun testResizeDouble_unconventionalSizes_compression() {
        val actual = ResizeDouble.resizeDouble(
            0x0A0C0B4448F5C28FL,
            4,
            27,
            1,
            13
        )
        Assert.assertEquals(0x3D7L, actual)
    }

    @Test
    fun testResizeDouble_unconventionalSizes_expansion() {
        val actual = ResizeDouble.resizeDouble(
            0x0A0C0B4448F5C28FL,
            2,
            7,
            10,
            30
        )
        Assert.assertEquals(0x17FC7800000L, actual)
    }

    @Test
    fun testResizeDouble_compressAndExpand() {
        val longVal = Double.doubleToRawLongBits(179.1357)
        val compressed = ResizeDouble.resizeDouble(longVal, 11, 52, 4, 31)
        val expanded = ResizeDouble.resizeDouble(compressed, 4, 31, 11, 52)
        val doubleExpanded = Double.longBitsToDouble(expanded)
        Assert.assertEquals(179.1356999874115, doubleExpanded, 0.0)
    }
}
