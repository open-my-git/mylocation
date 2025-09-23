package akhoi.libs.mlct.tools.java;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ByteConcatReaderTest {
    @Test
    public void testReadInt_zeroSize() {
        byte[] content = new byte[]{0x77};
        ByteConcatReader reader = new ByteConcatReader(content);
        int actual = reader.readInt(0);
        assertEquals(0, actual);
    }

    @Test
    public void testReadInt_partialByte() {
        byte[] content = new byte[]{0x77};
        ByteConcatReader reader = new ByteConcatReader(content);
        int actual = reader.readInt(5);
        assertEquals(0x0E, actual);
    }

    @Test
    public void testReadInt_fullByte() {
        byte[] content = new byte[]{0x77, 0x33};
        ByteConcatReader reader = new ByteConcatReader(content);
        int actual = reader.readInt(8);
        assertEquals(0x77, actual);
    }

    @Test
    public void testReadInt_oneAndAPartialBytes() {
        byte[] content = new byte[]{0x77, 0x33};
        ByteConcatReader reader = new ByteConcatReader(content);
        int actual = reader.readInt(15);
        assertEquals(0x3B99, actual);
    }

    @Test
    public void testReadInt_multipleBytes() {
        byte[] content = new byte[]{0x77, 0x33, 0x11};
        ByteConcatReader reader = new ByteConcatReader(content);
        int actual = reader.readInt(16);
        assertEquals(0x7733, actual);
    }

    @Test
    public void testReadInt_fullInteger() {
        byte[] content = new byte[]{0x77, 0x33, 0x11, 0x55, 0x00};
        ByteConcatReader reader = new ByteConcatReader(content);
        int actual = reader.readInt(32);
        assertEquals(0x77331155, actual);
    }

    @Test
    public void testReadInt_multipleReads() {
        byte[] content = new byte[]{0x77, 0x33};
        ByteConcatReader reader = new ByteConcatReader(content);
        int firstRead = reader.readInt(5);
        assertEquals(0x0E, firstRead);
        int secondRead = reader.readInt(10);
        assertEquals(0x399, secondRead);
    }

    @Test
    public void testReset() {
        byte[] content = new byte[]{0x77, 0x33};
        ByteConcatReader reader = new ByteConcatReader(content);
        int firstRead = reader.readInt(5);
        assertEquals(0x0E, firstRead);
        reader.reset();
        int secondRead = reader.readInt(15);
        assertEquals(0x3B99, secondRead);
    }

    @Test
    public void testReadLong_full() {
        ByteConcatReader reader = new ByteConcatReader(new byte[]{
                0x2E, (byte) 0xF1, 0x02, 0x36,
                (byte) 0xBD, 0x21, (byte) 0xFC, 0x77
        });
        long actual = reader.readLong(64);
        assertEquals(0x2EF10236BD21FC77L, actual);
    }

    @Test
    public void testReadLong_partial() {
        ByteConcatReader reader = new ByteConcatReader(new byte[]{
                0x2E, (byte) 0xF1, 0x02, 0x36,
                (byte) 0xBD, 0x21, (byte) 0xFC, 0x77
        });
        long actual = reader.readLong(46);
        assertEquals(0x0BBC408DAF48L, actual);
    }

    @Test
    public void testReadLong_integerSize() {
        ByteConcatReader reader = new ByteConcatReader(new byte[]{
                0x2E, (byte) 0xF1, 0x02, 0x36,
                (byte) 0xBD, 0x21, (byte) 0xFC, 0x77
        });
        long actual = reader.readLong(32);
        assertEquals(0x2EF10236L, actual);
    }

    @Test
    public void testReadLong_partialIntegerSize() {
        ByteConcatReader reader = new ByteConcatReader(new byte[]{
                0x2E, (byte) 0xF1, 0x02, 0x36,
                (byte) 0xBD, 0x21, (byte) 0xFC, 0x77
        });
        long actual = reader.readLong(23);
        assertEquals(0x177881L, actual);
    }

    @Test
    public void testReadLong_zeroSize() {
        ByteConcatReader reader = new ByteConcatReader(new byte[]{
                0x2E, (byte) 0xF1, 0x02, 0x36,
                (byte) 0xBD, 0x21, (byte) 0xFC, 0x77
        });
        long actual = reader.readLong(0);
        assertEquals(0L, actual);
    }

    @Test
    public void testReadLong_afterReadInt() {
        ByteConcatReader reader = new ByteConcatReader(new byte[]{
                0x02, 0x36, (byte) 0xBD, 0x21,
                (byte) 0xFC, 0x77, (byte) 0xFA, 0x16
        });
        int intNum = reader.readInt(25);
        long longNum = reader.readLong(32);
        assertEquals(0x46D7AL, intNum);
        assertEquals(0x43F8EFF4L, longNum);
    }

    @Test
    public void testReadInt_afterReadLong() {
        ByteConcatReader reader = new ByteConcatReader(new byte[]{
                0x02, 0x36, (byte) 0xBD, 0x21,
                (byte) 0xFC, 0x77, (byte) 0xFA, 0x16
        });
        long longNum = reader.readLong(32);
        int intNum = reader.readInt(25);
        assertEquals(0x236BD21L, longNum);
        assertEquals(0x1F8EFF4, intNum);
    }

    @Test
    public void testReadInt_exceededContentLength() {
        ByteConcatReader reader = new ByteConcatReader(new byte[]{
                0x62, 0x1F, (byte) 0xBC
        });
        int actual = reader.readInt(32);
        assertEquals(0x621FBC, actual);
    }

    @Test
    public void testReadLong_exceededNumberSize() {
        ByteConcatReader reader = new ByteConcatReader(new byte[]{
                0x62, 0x1F, (byte) 0xBC, (byte) 0xFF,
                0x30, (byte) 0xAD, (byte) 0xFC, 0x14,
                0x44, (byte) 0x95
        });
        long actual = reader.readLong(65);
        assertEquals(0x621FBCFF30ADFC14L, actual);
    }

    @Test
    public void testReadDouble() {
        ByteConcatReader reader = new ByteConcatReader(new byte[]{
                0x23, (byte) 0xFE, (byte) 0xB7, 0x09,
                0x12, (byte) 0x98, (byte) 0xF1, (byte) 0xDC
        });
        double actual = reader.readDouble(64);
        assertEquals(0x23FEB7091298F1DCL, Double.doubleToRawLongBits(actual));
    }

    @Test
    public void testSkip() {
        ByteConcatReader reader = new ByteConcatReader(new byte[]{0x23, (byte) 0xFE});
        int firstRead = reader.readInt(4);
        reader.skip(3);
        long secondRead = reader.readLong(2);
        assertEquals(0x2, firstRead);
        assertEquals(0x3, secondRead);
    }
}
