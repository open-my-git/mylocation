package akhoi.libs.mlct.tools.bcc

import org.junit.Assert
import org.junit.Test
import java.util.Random

class ByteConcatTest {
    @Test
    fun testAppendInt_positive_full() {
        val byteConcat = ByteConcat(4)
        byteConcat.appendInt(0x03A44718, 32)

        val expected = byteArrayOf(0x03, 0xA4.toByte(), 0x47, 0x18)
        Assert.assertArrayEquals(expected, byteConcat.getContent())
    }

    @Test
    fun testAppendInt_positive_partial() {
        val byteConcat = ByteConcat(1)
        byteConcat.appendInt(0x18, 4)

        val expected = byteArrayOf(0x80.toByte())
        Assert.assertArrayEquals(expected, byteConcat.getContent())
    }

    @Test
    fun testAppendInt_negative_partial() {
        val byteConcat = ByteConcat(2)
        byteConcat.appendInt(-0x4D2, 12)

        val expected = byteArrayOf(0xB2.toByte(), 0xE0.toByte())
        Assert.assertArrayEquals(expected, byteConcat.getContent())
    }

    @Test
    fun testAppendInt_negative_full() {
        val byteConcat = ByteConcat(4)
        byteConcat.appendInt(-0x03A44718, 32)

        val expected = byteArrayOf(0xFC.toByte(), 0x5B, 0xB8.toByte(), 0xE8.toByte())
        Assert.assertArrayEquals(expected, byteConcat.getContent())
    }

    @Test
    fun testAppendInt_full_random() {
        val count: Int = RANDOM.nextInt(20)
        for (i in 0..<count) {
            val value: Int = RANDOM.nextInt()
            val byteConcat = ByteConcat(4)
            byteConcat.appendInt(value, 32)

            val expected = ByteArray(4)
            for (j in 0..3) {
                expected[j] = (value shr ((3 - j) * 8)).toByte()
            }
            Assert.assertArrayEquals(expected, byteConcat.getContent())
        }
    }

    @Test
    fun testAppendInt_multiple_partial_noRemainder() {
        val byteConcat = ByteConcat(6)
        byteConcat.appendInt(0x180, 8)
        byteConcat.appendInt(0x1814C, 16)
        byteConcat.appendInt(0x1A49168, 24)

        val expected = byteArrayOf(
            0x80.toByte(), 0x81.toByte(), 0x4C, 0xA4.toByte(), 0x91.toByte(), 0x68
        )
        Assert.assertArrayEquals(expected, byteConcat.getContent())
    }

    @Test
    fun testAppendInt_multiple_partial_havingRemainder() {
        val byteConcat = ByteConcat(8)
        byteConcat.appendInt(0x39446, 12)
        byteConcat.appendInt(0x35077C01, 20)
        byteConcat.appendInt(0x29273C60, 28)

        val expected = byteArrayOf(
            0x44, 0x67, 0x7C, 0x01,
            0x92.toByte(), 0x73, 0xC6.toByte(), 0x00
        )
        Assert.assertArrayEquals(expected, byteConcat.getContent())
    }

    @Test
    fun testAppendInt_multiple_withinOneByte() {
        val byteConcat = ByteConcat(1)
        byteConcat.appendInt(1, 1)
        byteConcat.appendInt(1, 1)
        byteConcat.appendInt(1, 1)
        Assert.assertArrayEquals(byteArrayOf(0xE0.toByte()), byteConcat.getContent())
    }

    @Test
    fun testAppendInt_zeroValueSize() {
        val byteConcat = ByteConcat(3)
        byteConcat.appendInt(0xC22A84, 0)
        Assert.assertArrayEquals(ByteArray(0), byteConcat.getContent())
    }

    @Test
    fun testAppendInt_negativeSize() {
        val byteConcat = ByteConcat()
        byteConcat.appendInt(0x01, -10)
        Assert.assertArrayEquals(ByteArray(0), byteConcat.getContent())
    }

    @Test
    fun testAppendInt_bucketExpanded() {
        val byteConcat = ByteConcat(3)
        byteConcat.appendInt(0x22222222, 32)
        val expected = byteArrayOf(0x22, 0x22, 0x22, 0x22)
        Assert.assertArrayEquals(expected, byteConcat.getContent())
        Assert.assertEquals(4, byteConcat.getContentSize().toLong())
    }

    @Test
    fun testAppendInt_bucketExpanded_multipleTimes() {
        val byteConcat = ByteConcat(1)
        byteConcat.appendInt(0x22222222, 32)
        val expected = byteArrayOf(0x22, 0x22, 0x22, 0x22)
        Assert.assertArrayEquals(expected, byteConcat.getContent())
        Assert.assertEquals(4, byteConcat.getContentSize().toLong())
    }

    @Test(expected = OutOfMemoryError::class)
    fun testAppendInt_bucketSizeTooLarge() {
        val byteConcat = ByteConcat(1024)
        while (true) {
            byteConcat.appendInt(0x01, 32)
        }
    }

    @Test
    fun testAppendLong_multiple_withinOneByte() {
        val byteConcat = ByteConcat(1)
        byteConcat.appendLong(-0x01, 2)
        byteConcat.appendLong(0x23, 2)
        byteConcat.appendLong(0x45, 2)
        Assert.assertArrayEquals(byteArrayOf(0xF4.toByte()), byteConcat.getContent())
    }

    @Test
    fun testAppendLong_full() {
        val byteConcat = ByteConcat(8)
        byteConcat.appendLong(0x1896014701634004L, 64)
        Assert.assertArrayEquals(
            byteArrayOf(
                0x18, 0x96.toByte(), 0x01, 0x47, 0x01, 0x63, 0x40, 0x04
            ), byteConcat.getContent()
        )
    }

    @Test
    fun testAppendLong_multiple_partial() {
        val byteConcat = ByteConcat(11)
        byteConcat.appendLong(0x1896014701634004L, 64)
        byteConcat.appendLong(0x1896014701634004L, 24)
        Assert.assertArrayEquals(
            byteArrayOf(
                0x18, 0x96.toByte(), 0x01, 0x47, 0x01, 0x63, 0x40, 0x04, 0x63, 0x40, 0x04
            ), byteConcat.getContent()
        )
    }

    @Test
    fun testAppendLong_full_random() {
        val count: Int = RANDOM.nextInt(20)
        for (i in 0..<count) {
            val value: Long = RANDOM.nextLong()
            val byteConcat = ByteConcat(8)
            byteConcat.appendLong(value, 64)

            val expected = ByteArray(8)
            for (j in 0..7) {
                expected[j] = (value shr ((7 - j) * 8)).toByte()
            }
            Assert.assertArrayEquals(expected, byteConcat.getContent())
        }
    }

    @Test
    fun testAppendLong_positiveInteger_integerSize() {
        val byteConcat = ByteConcat(3)
        byteConcat.appendLong(0x10460E10L, 17)
        Assert.assertArrayEquals(byteArrayOf(0x07, 0x08, 0x00), byteConcat.getContent())
    }

    @Test
    fun testAppendLong_negativeInteger_integerSize() {
        val byteConcat = ByteConcat(3)
        byteConcat.appendLong(-0x10460E10L, 17)
        Assert.assertArrayEquals(
            byteArrayOf(0xF8.toByte(), 0xF8.toByte(), 0x00),
            byteConcat.getContent()
        )
    }

    @Test
    fun testAppendLong_positiveLong_integerSize() {
        val byteConcat = ByteConcat(4)
        byteConcat.appendLong(0x1896014701634004L, 25)
        Assert.assertArrayEquals(
            byteArrayOf(
                0xB1.toByte(), 0xA0.toByte(), 0x02, 0x00
            ), byteConcat.getContent()
        )
    }

    @Test
    fun testAppendLong_negativeLong_integerSize() {
        val byteConcat = ByteConcat(4)
        byteConcat.appendLong(-0x1896014701634004L, 25)
        Assert.assertArrayEquals(
            byteArrayOf(
                0x4E, 0x5F, 0xFE.toByte(), 0x00
            ), byteConcat.getContent()
        )
    }

    @Test
    fun testAppendLong_positiveLong_longSize() {
        val byteConcat = ByteConcat(6)
        byteConcat.appendLong(0x1896014701634004L, 43)
        Assert.assertArrayEquals(
            byteArrayOf(
                0x28, 0xE0.toByte(), 0x2C, 0x68, 0x00, 0x80.toByte()
            ), byteConcat.getContent()
        )
    }

    @Test
    fun testAppendLong_negativeLong_longSize() {
        val byteConcat = ByteConcat(6)
        byteConcat.appendLong(-0x1896014701634004L, 43)
        Assert.assertArrayEquals(
            byteArrayOf(
                0xD7.toByte(), 0x1F, 0xD3.toByte(), 0x97.toByte(), 0xFF.toByte(), 0x80.toByte()
            ), byteConcat.getContent()
        )
    }

    @Test
    fun testAppendLong_bucketExpanded() {
        val byteConcat = ByteConcat(1)
        byteConcat.appendLong(0x1896014701634004L, 64)
        Assert.assertArrayEquals(
            byteArrayOf(
                0x18, 0x96.toByte(), 0x01, 0x47, 0x01, 0x63, 0x40, 0x04
            ), byteConcat.getContent()
        )
        Assert.assertEquals(8, byteConcat.getContentSize().toLong())
    }

    @Test
    fun testConstructor_negativeInitCap() {
        val byteConcat = ByteConcat(-15)
        Assert.assertEquals(2, byteConcat.getContentSize().toLong())
    }

    companion object {
        private val RANDOM = Random()
    }
}
