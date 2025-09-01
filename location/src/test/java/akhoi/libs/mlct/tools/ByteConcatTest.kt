package akhoi.libs.mlct.tools

import org.junit.Test
import kotlin.random.Random
import kotlin.test.assertContentEquals

class ByteConcatTest {
    @Test
    fun testAppendInt_positive_full() {
        val byteConcat = ByteConcat(4)
        byteConcat.appendInt(0x3A44718, 32)

        val expected = byteArrayOf(0x03, 0xA4.toByte(), 0x47, 0x18)
        assertContentEquals(expected, byteConcat.getContent())
    }

    @Test
    fun testAppendInt_positive_partial() {
        val byteConcat = ByteConcat(1)
        byteConcat.appendInt(0x18, 4)

        val expected = byteArrayOf(0x80.toByte())
        assertContentEquals(expected, byteConcat.getContent())
    }

    @Test
    fun testAppendInt_negative_partial() {
        val byteConcat = ByteConcat(2)
        byteConcat.appendInt(-0x4D2, 12)

        val expected = byteArrayOf(0xB2.toByte(), 0xE0.toByte())
        assertContentEquals(expected, byteConcat.getContent())
    }

    @Test
    fun testAppendInt_negative_full() {
        val byteConcat = ByteConcat(4)
        byteConcat.appendInt(-0x3A44718, 32)

        val expected = byteArrayOf(0xFC.toByte(), 0x5B, 0xB8.toByte(), 0xE8.toByte())
        assertContentEquals(expected, byteConcat.getContent())
    }

    @Test
    fun testAppendInt_multiple_partial_aligned() {
        val byteConcat = ByteConcat(6)
        byteConcat.appendInt(0x180, 8)
        byteConcat.appendInt(0x1814C, 16)
        byteConcat.appendInt(0x1A49168, 24)

        val expected = byteArrayOf(0x80.toByte(), 0x81.toByte(), 0x4C, 0xA4.toByte(), 0x91.toByte(), 0x68)
        assertContentEquals(expected, byteConcat.getContent())
    }

    @Test
    fun testAppendInt_full_random() {
        val count = Random.nextInt(20)
        val values = IntArray(count) { Random.nextInt() }

        values.forEach { value ->
            val byteConcat = ByteConcat(4)
            byteConcat.appendInt(value, 32)

            val expected = ByteArray(4) { i -> (value shr ((3 - i) * 8)).toByte() }
            assertContentEquals(expected, byteConcat.getContent())
        }
    }

    @Test
    fun testAppendInt_multiple_partial_unaligned() {
        val byteConcat = ByteConcat(8)
        byteConcat.appendInt(0x39446, 12)
        byteConcat.appendInt(0x35077C01, 20)
        byteConcat.appendInt(0x29273C60, 28)

        val expected = byteArrayOf(
            0x44, 0x67, 0x7C, 0x01,
            0x92.toByte(), 0x73, 0xC6.toByte(), 0x00
        )

        assertContentEquals(expected, byteConcat.getContent())
    }

    @Test
    fun testAppendInt_zeroValueSize() {
        val byteConcat = ByteConcat(3)
        byteConcat.appendInt(0xC22A84, 0)
        assertContentEquals(ByteArray(0), byteConcat.getContent())
    }

    @Test
    fun testAppendInt_bucketExpanded() {
        val byteConcat = ByteConcat(3)
        byteConcat.appendInt(0x22222222, 32)
        val expected = byteArrayOf(0x22, 0x22, 0x22, 0x22)
        assertContentEquals(expected, byteConcat.getContent())
    }

    @Test
    fun testAppendInt_bucketExpanded_multipleTimes() {
        val byteConcat = ByteConcat(1)
        byteConcat.appendInt(0x22222222, 32)
        val expected = byteArrayOf(0x22, 0x22, 0x22, 0x22)
        assertContentEquals(expected, byteConcat.getContent())
    }

    @Test
    fun testAppendInt_multiple_withinOneByte() {
        val byteConcat = ByteConcat(1)
        byteConcat.appendInt(1, 1)
        byteConcat.appendInt(1, 1)
        byteConcat.appendInt(1, 1)
        assertContentEquals(byteArrayOf(0xE0.toByte()), byteConcat.getContent())
    }

    @Test
    fun testAppendInt_negativeSize() {
        val byteConcat = ByteConcat()
        byteConcat.appendInt(0x01, -10)
        assertContentEquals(byteArrayOf(), byteConcat.getContent())
    }

    @Test(expected = OutOfMemoryError::class)
    fun testAppendInt_bucketSizeTooLarge() {
        val byteConcat = ByteConcat(1000)
        while (true) {
            byteConcat.appendInt(0x01, 32)
        }
    }

    @Test
    fun testAppendLong_multiple_withinOneByte() {
        val byteConcat = ByteConcat(1)
        byteConcat.appendLong(0x01, 2)
        byteConcat.appendLong(0x23, 2)
        byteConcat.appendLong(0x45, 2)
        assertContentEquals(
            byteArrayOf(0x74),
            byteConcat.getContent()
        )
    }

    @Test
    fun testAppendLong_full() {
        val byteConcatLong = ByteConcat(8)
        byteConcatLong.appendLong(0x1896014701634004, 64)
        assertContentEquals(
            byteArrayOf(0x18, 0x96.toByte(), 0x01, 0x47, 0x01, 0x63, 0x40, 0x04),
            byteConcatLong.getContent()
        )
    }

    @Test
    fun testAppendLong_full_random() {
        val count = Random.nextInt(20)
        val values = LongArray(count) { Random.nextLong() }

        values.forEach { value ->
            val byteConcat = ByteConcat(8)
            byteConcat.appendLong(value, 64)

            val expected = ByteArray(8) { i -> (value shr ((7 - i) * 8)).toByte() }
            assertContentEquals(expected, byteConcat.getContent())
        }
    }

    @Test
    fun testAppendLong_positiveInteger_integerSize() {
        val byteConcatLong = ByteConcat(3)
        byteConcatLong.appendLong(0x10460E10L, 17)
        assertContentEquals(byteArrayOf(0x07, 0x08, 0x00), byteConcatLong.getContent())
    }

    @Test
    fun testAppendLong_negativeInteger_integerSize() {
        val byteConcatLong = ByteConcat(3)
        byteConcatLong.appendLong(-0x10460E10L, 17)
        assertContentEquals(byteArrayOf(0xF8.toByte(), 0xF8.toByte(), 0x00), byteConcatLong.getContent())
    }

    @Test
    fun testAppendLong_positiveLong_integerSize() {
        val byteConcatLong = ByteConcat(4)
        byteConcatLong.appendLong(0x1896014701634004, 25)
        assertContentEquals(
            byteArrayOf(0xB1.toByte(), 0xA0.toByte(), 0x02, 0x00),
            byteConcatLong.getContent()
        )
    }

    @Test
    fun testAppendLong_negativeLong_integerSize() {
        val byteConcatLong = ByteConcat(4)
        byteConcatLong.appendLong(-0x1896014701634004, 25)
        assertContentEquals(
            byteArrayOf(0x4E, 0x5F, 0xFE.toByte(), 0x00),
            byteConcatLong.getContent()
        )
    }

    @Test
    fun testAppendLong_positiveLong_longSize() {
        val byteConcatLong = ByteConcat(6)
        byteConcatLong.appendLong(0x1896014701634004, 43)
        assertContentEquals(
            byteArrayOf(0x28, 0xE0.toByte(), 0x2C, 0x68, 0x00, 0x80.toByte()),
            byteConcatLong.getContent()
        )
    }

    @Test
    fun testAppendLong_negativeLong_longSize() {
        val byteConcatLong = ByteConcat(6)
        byteConcatLong.appendLong(-0x1896014701634004, 43)
        assertContentEquals(
            byteArrayOf(0xD7.toByte(), 0x1F, 0xD3.toByte(), 0x97.toByte(), 0xFF.toByte(), 0x80.toByte()),
            byteConcatLong.getContent()
        )
    }

    @Test
    fun testAppendLong_bucketExpanded() {
        val byteConcat = ByteConcat(1)
        byteConcat.appendLong(0x1896014701634004, 64)
        assertContentEquals(
            byteArrayOf(0x18, 0x96.toByte(), 0x01, 0x47, 0x01, 0x63, 0x40, 0x04),
            byteConcat.getContent()
        )
    }

}