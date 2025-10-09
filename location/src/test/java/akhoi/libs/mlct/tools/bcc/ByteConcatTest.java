package akhoi.libs.mlct.tools.bcc;

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ByteConcatTest {
    private static final Random RANDOM = new Random();

    @Test
    public void testAppendInt_positive_full() {
        ByteConcat byteConcat = new ByteConcat(4);
        byteConcat.appendInt(0x03A44718, 32);

        byte[] expected = new byte[]{0x03, (byte) 0xA4, 0x47, 0x18};
        assertArrayEquals(expected, byteConcat.getContent());
    }

    @Test
    public void testAppendInt_positive_partial() {
        ByteConcat byteConcat = new ByteConcat(1);
        byteConcat.appendInt(0x18, 4);

        byte[] expected = new byte[]{(byte) 0x80};
        assertArrayEquals(expected, byteConcat.getContent());
    }

    @Test
    public void testAppendInt_negative_partial() {
        ByteConcat byteConcat = new ByteConcat(2);
        byteConcat.appendInt(-0x4D2, 12);

        byte[] expected = new byte[]{(byte) 0xB2, (byte) 0xE0};
        assertArrayEquals(expected, byteConcat.getContent());
    }

    @Test
    public void testAppendInt_negative_full() {
        ByteConcat byteConcat = new ByteConcat(4);
        byteConcat.appendInt(-0x03A44718, 32);

        byte[] expected = new byte[]{(byte) 0xFC, 0x5B, (byte) 0xB8, (byte) 0xE8};
        assertArrayEquals(expected, byteConcat.getContent());
    }

    @Test
    public void testAppendInt_full_random() {
        int count = RANDOM.nextInt(20);
        for (int i = 0; i < count; i++) {
            int value = RANDOM.nextInt();
            ByteConcat byteConcat = new ByteConcat(4);
            byteConcat.appendInt(value, 32);

            byte[] expected = new byte[4];
            for (int j = 0; j < 4; j++) {
                expected[j] = (byte) (value >> ((3 - j) * 8));
            }
            assertArrayEquals(expected, byteConcat.getContent());
        }
    }

    @Test
    public void testAppendInt_multiple_partial_noRemainder() {
        ByteConcat byteConcat = new ByteConcat(6);
        byteConcat.appendInt(0x180, 8);
        byteConcat.appendInt(0x1814C, 16);
        byteConcat.appendInt(0x1A49168, 24);

        byte[] expected = new byte[]{
                (byte) 0x80, (byte) 0x81, 0x4C, (byte) 0xA4, (byte) 0x91, 0x68
        };
        assertArrayEquals(expected, byteConcat.getContent());
    }

    @Test
    public void testAppendInt_multiple_partial_havingRemainder() {
        ByteConcat byteConcat = new ByteConcat(8);
        byteConcat.appendInt(0x39446, 12);
        byteConcat.appendInt(0x35077C01, 20);
        byteConcat.appendInt(0x29273C60, 28);

        byte[] expected = new byte[]{
                0x44, 0x67, 0x7C, 0x01,
                (byte) 0x92, 0x73, (byte) 0xC6, 0x00
        };
        assertArrayEquals(expected, byteConcat.getContent());
    }

    @Test
    public void testAppendInt_multiple_withinOneByte() {
        ByteConcat byteConcat = new ByteConcat(1);
        byteConcat.appendInt(1, 1);
        byteConcat.appendInt(1, 1);
        byteConcat.appendInt(1, 1);
        assertArrayEquals(new byte[]{(byte) 0xE0}, byteConcat.getContent());
    }

    @Test
    public void testAppendInt_zeroValueSize() {
        ByteConcat byteConcat = new ByteConcat(3);
        byteConcat.appendInt(0xC22A84, 0);
        assertArrayEquals(new byte[0], byteConcat.getContent());
    }

    @Test
    public void testAppendInt_negativeSize() {
        ByteConcat byteConcat = new ByteConcat();
        byteConcat.appendInt(0x01, -10);
        assertArrayEquals(new byte[0], byteConcat.getContent());
    }

    @Test
    public void testAppendInt_bucketExpanded() {
        ByteConcat byteConcat = new ByteConcat(3);
        byteConcat.appendInt(0x22222222, 32);
        byte[] expected = new byte[]{0x22, 0x22, 0x22, 0x22};
        assertArrayEquals(expected, byteConcat.getContent());
        assertEquals(4, byteConcat.getContentSize());
    }

    @Test
    public void testAppendInt_bucketExpanded_multipleTimes() {
        ByteConcat byteConcat = new ByteConcat(1);
        byteConcat.appendInt(0x22222222, 32);
        byte[] expected = new byte[]{0x22, 0x22, 0x22, 0x22};
        assertArrayEquals(expected, byteConcat.getContent());
        assertEquals(4, byteConcat.getContentSize());
    }

    @Test(expected = OutOfMemoryError.class)
    public void testAppendInt_bucketSizeTooLarge() {
        ByteConcat byteConcat = new ByteConcat(1024);
        while (true) {
            byteConcat.appendInt(0x01, 32);
        }
    }

    @Test
    public void testAppendLong_multiple_withinOneByte() {
        ByteConcat byteConcat = new ByteConcat(1);
        byteConcat.appendLong(-0x01, 2);
        byteConcat.appendLong(0x23, 2);
        byteConcat.appendLong(0x45, 2);
        assertArrayEquals(new byte[]{(byte) 0xF4}, byteConcat.getContent());
    }

    @Test
    public void testAppendLong_full() {
        ByteConcat byteConcat = new ByteConcat(8);
        byteConcat.appendLong(0x1896014701634004L, 64);
        assertArrayEquals(new byte[]{
                0x18, (byte) 0x96, 0x01, 0x47, 0x01, 0x63, 0x40, 0x04
        }, byteConcat.getContent());
    }

    @Test
    public void testAppendLong_multiple_partial() {
        ByteConcat byteConcat = new ByteConcat(11);
        byteConcat.appendLong(0x1896014701634004L, 64);
        byteConcat.appendLong(0x1896014701634004L, 24);
        assertArrayEquals(new byte[]{
                0x18, (byte) 0x96, 0x01, 0x47, 0x01, 0x63, 0x40, 0x04, 0x63, 0x40, 0x04
        }, byteConcat.getContent());
    }

    @Test
    public void testAppendLong_full_random() {
        int count = RANDOM.nextInt(20);
        for (int i = 0; i < count; i++) {
            long value = RANDOM.nextLong();
            ByteConcat byteConcat = new ByteConcat(8);
            byteConcat.appendLong(value, 64);

            byte[] expected = new byte[8];
            for (int j = 0; j < 8; j++) {
                expected[j] = (byte) (value >> ((7 - j) * 8));
            }
            assertArrayEquals(expected, byteConcat.getContent());
        }
    }

    @Test
    public void testAppendLong_positiveInteger_integerSize() {
        ByteConcat byteConcat = new ByteConcat(3);
        byteConcat.appendLong(0x10460E10L, 17);
        assertArrayEquals(new byte[]{0x07, 0x08, 0x00}, byteConcat.getContent());
    }

    @Test
    public void testAppendLong_negativeInteger_integerSize() {
        ByteConcat byteConcat = new ByteConcat(3);
        byteConcat.appendLong(-0x10460E10L, 17);
        assertArrayEquals(new byte[]{(byte) 0xF8, (byte) 0xF8, 0x00}, byteConcat.getContent());
    }

    @Test
    public void testAppendLong_positiveLong_integerSize() {
        ByteConcat byteConcat = new ByteConcat(4);
        byteConcat.appendLong(0x1896014701634004L, 25);
        assertArrayEquals(new byte[]{
                (byte) 0xB1, (byte) 0xA0, 0x02, 0x00
        }, byteConcat.getContent());
    }

    @Test
    public void testAppendLong_negativeLong_integerSize() {
        ByteConcat byteConcat = new ByteConcat(4);
        byteConcat.appendLong(-0x1896014701634004L, 25);
        assertArrayEquals(new byte[]{
                0x4E, 0x5F, (byte) 0xFE, 0x00
        }, byteConcat.getContent());
    }

    @Test
    public void testAppendLong_positiveLong_longSize() {
        ByteConcat byteConcat = new ByteConcat(6);
        byteConcat.appendLong(0x1896014701634004L, 43);
        assertArrayEquals(new byte[]{
                0x28, (byte) 0xE0, 0x2C, 0x68, 0x00, (byte) 0x80
        }, byteConcat.getContent());
    }

    @Test
    public void testAppendLong_negativeLong_longSize() {
        ByteConcat byteConcat = new ByteConcat(6);
        byteConcat.appendLong(-0x1896014701634004L, 43);
        assertArrayEquals(new byte[]{
                (byte) 0xD7, 0x1F, (byte) 0xD3, (byte) 0x97, (byte) 0xFF, (byte) 0x80
        }, byteConcat.getContent());
    }

    @Test
    public void testAppendLong_bucketExpanded() {
        ByteConcat byteConcat = new ByteConcat(1);
        byteConcat.appendLong(0x1896014701634004L, 64);
        assertArrayEquals(new byte[]{
                0x18, (byte) 0x96, 0x01, 0x47, 0x01, 0x63, 0x40, 0x04
        }, byteConcat.getContent());
        assertEquals(8, byteConcat.getContentSize());
    }

    @Test
    public void testConstructor_negativeInitCap() {
        ByteConcat byteConcat = new ByteConcat(-15);
        assertEquals(2, byteConcat.getContentSize());
    }
}
