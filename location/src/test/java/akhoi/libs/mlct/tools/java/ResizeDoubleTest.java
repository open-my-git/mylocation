package akhoi.libs.mlct.tools.java;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ResizeDoubleTest {
    @Test
    public void testCompressDouble() {
        long actual = ResizeDouble.compressDouble(3.14, 4, 31);
        assertEquals(0x448F5C28FL, actual);
    }

    @Test
    public void testCompressDouble_expSizeOne() {
        long actual = ResizeDouble.compressDouble(56.18, 1, 20);
        assertEquals(0x1C170AL, actual);
    }

    @Test
    public void testCompressDouble_expSizeZero() {
        long actual = ResizeDouble.compressDouble(56.18, 0, 20);
        assertEquals(0x0C170AL, actual);
    }

    @Test
    public void testCompressDouble_negativeExpSize() {
        long actual = ResizeDouble.compressDouble(56.18, -8, 20);
        assertEquals(0x0C170AL, actual);
    }

    @Test
    public void testCompressDouble_sigSizeOne() {
        long actual = ResizeDouble.compressDouble(56.18, 5, 1);
        assertEquals(0x29L, actual);
    }

    @Test
    public void testCompressDouble_sigSizeZero() {
        long actual = ResizeDouble.compressDouble(56.18, 5, 0);
        assertEquals(0x29L, actual);
    }

    @Test
    public void testCompressDouble_negativeSigSize() {
        long actual = ResizeDouble.compressDouble(56.18, 5, -3);
        assertEquals(0x29L, actual);
    }

    @Test
    public void testCompressDouble_positiveSubnormal() {
        long actual = ResizeDouble.compressDouble(1.1508711201542864e-308, 5, 3);
        assertEquals(0x4L, actual);
    }

    @Test
    public void testCompressDouble_negativeSubnormal() {
        long actual = ResizeDouble.compressDouble(-1.1508711201542864e-308, 5, 3);
        assertEquals(0x104L, actual);
    }

    @Test
    public void testCompressDouble_positiveInfinity() {
        long actual = ResizeDouble.compressDouble(Double.POSITIVE_INFINITY, 3, 7);
        assertEquals(0x380L, actual);
    }

    @Test
    public void testCompressDouble_negativeInfinity() {
        long actual = ResizeDouble.compressDouble(Double.NEGATIVE_INFINITY, 3, 7);
        assertEquals(0x780L, actual);
    }

    @Test
    public void testCompressDouble_positiveZero() {
        long actual = ResizeDouble.compressDouble(0.0, 5, 2);
        assertEquals(0L, actual);
    }

    @Test
    public void testCompressDouble_negativeZero() {
        long actual = ResizeDouble.compressDouble(-0.0, 5, 2);
        assertEquals(0x80L, actual);
    }

    @Test
    public void testExpandDouble_partialDoubleSize() {
        double actual = ResizeDouble.expandDouble(0x448F5C28FL, 4, 31);
        assertEquals(3.139999999664724, actual, 0.0);
    }

    @Test
    public void testExpandDouble_fullDoubleSize() {
        double actual = ResizeDouble.expandDouble(0x40091EB851EB851FL, 11, 52);
        assertEquals(3.14, actual, 0.0);
    }

    @Test
    public void testExpandDouble_zeroExpSize() {
        double actual = ResizeDouble.expandDouble(0x0F8F5C28F5C29L, 0, 52);
        assertEquals(Double.longBitsToDouble(0x4000F8F5C28F5C29L), actual, 0.0);
    }

    @Test
    public void testResizeDouble_subnormal() {
        long actual = ResizeDouble.resizeDouble(0x8C80029318421L, 11, 52, 3, 52);
        assertEquals(0x8C80029318421L, actual);
    }

    @Test
    public void testResizeDouble_NaN() {
        long actual = ResizeDouble.resizeDouble(0x7FF0000000000001L, 11, 52, 5, 10);
        assertEquals(0x7C00L, actual);
    }

    @Test
    public void testResizeDouble_havingUnwantedLeadingBits() {
        long actual = ResizeDouble.resizeDouble(0x0A0C0B4448F5C28FL, 4, 31, 11, 52);
        assertEquals(0x40091EB851E00000L, actual);
    }

    @Test
    public void testResizeDouble_unconventionalSizes_compression() {
        long actual = ResizeDouble.resizeDouble(
                0x0A0C0B4448F5C28FL,
                4,
                27,
                1,
                13
        );
        assertEquals(0x3D7L, actual);
    }

    @Test
    public void testResizeDouble_unconventionalSizes_expansion() {
        long actual = ResizeDouble.resizeDouble(
                0x0A0C0B4448F5C28FL,
                2,
                7,
                10,
                30
        );
        assertEquals(0x17FC7800000L, actual);
    }

    @Test
    public void testResizeDouble_compressAndExpand() {
        long longVal = Double.doubleToRawLongBits(179.1357);
        long compressed = ResizeDouble.resizeDouble(longVal, 11, 52, 4, 31);
        long expanded = ResizeDouble.resizeDouble(compressed, 4, 31, 11, 52);
        double doubleExpanded = Double.longBitsToDouble(expanded);
        assertEquals(179.1356999874115, doubleExpanded, 0.0);
    }
}
