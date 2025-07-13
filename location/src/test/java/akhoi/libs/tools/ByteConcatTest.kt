package akhoi.libs.tools

import org.junit.Test
import kotlin.random.Random
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class ByteConcatTest {
    @Test
    fun testAppendInt_positive_notTruncated() {
        val byteConcat = ByteConcat(4)
        byteConcat.appendInt(61097752, 32)

        val expected = byteArrayFromInts(0x03, 0xA4, 0x47, 0x18)
        assertContentEquals(expected, byteConcat.content)
        assertEquals(expected.size * 8, byteConcat.position)
    }

    @Test
    fun testAppendInt_positive_truncated() {
        val byteConcat = ByteConcat(1)
        byteConcat.appendInt(24, 4)

        val expected = byteArrayFromInts(0x80)
        assertContentEquals(expected, byteConcat.content)
        assertEquals(4, byteConcat.position)
    }

    @Test
    fun testAppendInt_negative_truncated() {
        val byteConcat = ByteConcat(2)
        byteConcat.appendInt(-1234, 12)

        val expected = byteArrayFromInts(0xB2, 0xE0)
        assertContentEquals(expected, byteConcat.content)
        assertEquals(12, byteConcat.position)
    }

    @Test
    fun testAppendInt_negative_notTruncated() {
        val byteConcat = ByteConcat(4)
        byteConcat.appendInt(-61097752, 32)

        val expected = byteArrayFromInts(0xFC, 0x5B, 0xB8, 0xE8)
        assertContentEquals(expected, byteConcat.content)
        assertEquals(expected.size * 8, byteConcat.position)
    }

    @Test
    fun testAppendInt_multiple_truncated_aligned() {
        val byteConcat = ByteConcat(6)
        byteConcat.appendInt(384, 8)
        byteConcat.appendInt(98636, 16)
        byteConcat.appendInt(27562344, 24)

        val expected = byteArrayFromInts(0x80, 0x81, 0x4C, 0xA4, 0x91, 0x68)
        assertContentEquals(expected, byteConcat.content)
        assertEquals(expected.size * 8, byteConcat.position)
    }

    @Test
    fun testAppendInt_random_notTruncated() {
        val size = Random.nextInt(20)
        val testCases = IntArray(size) { Random.nextInt() }

        testCases.forEach { value ->
            val byteConcat = ByteConcat(4)
            byteConcat.appendInt(value, 32)

            val expected = ByteArray(4)
            for (i in 0..3) {
                val byteIndex = 8 * (3 - i)
                expected[i] = ((value shl (24 - byteIndex)) shr 24).toByte()
            }

            assertContentEquals(expected, byteConcat.content)
            assertEquals(expected.size * 8, byteConcat.position)
        }
    }

    @Test
    fun testAppendInt_multiple_truncated_unaligned() {
        val byteConcat = ByteConcat(8)
        byteConcat.appendInt(234566, 12)
        byteConcat.appendInt(889682945, 20)
        byteConcat.appendInt(690437216, 28)

        val expected = byteArrayFromInts(
            0x44, 0x67, 0x7C, 0x01,
            0x92, 0x73, 0xC6, 0x00
        )

        assertContentEquals(expected, byteConcat.content)
        assertEquals(60, byteConcat.position)
    }

    @Test
    fun testAppendInt_zeroValueSize() {
        val byteConcat = ByteConcat(3)
        byteConcat.appendInt(12730100, 0)
        val expected = byteArrayOf(0, 0, 0)
        assertContentEquals(expected, byteConcat.content)
        assertEquals(0, byteConcat.position)
    }

    @Test
    fun testAppendInt_maxValueSize() {
        val byteConcat = ByteConcat(4)
        byteConcat.appendInt(1412317098, 32)
        val expected = byteArrayFromInts(0x54, 0x2E, 0x3F, 0xAA)
        assertContentEquals(expected, byteConcat.content)
        assertEquals(32, byteConcat.position)
    }

    @Test
    fun testAppendInt_sizeExceededCapacity() {
        val byteConcat = ByteConcat(3)
        byteConcat.appendInt(572662306, 32)
        val expected = byteArrayOf(0x22, 0x22, 0x22)
        assertContentEquals(expected, byteConcat.content)
        assertEquals(24, byteConcat.position)
    }

    @Test
    fun testAppendInt_multiple_withinOneByte() {
        val byteConcat = ByteConcat(1)
        byteConcat.appendInt(1, 1)
        byteConcat.appendInt(1, 1)
        byteConcat.appendInt(1, 1)
        assertContentEquals(byteArrayOf(0xE0.toByte()), byteConcat.content)
    }

    private fun byteArrayFromInts(vararg ints: Int) = ints.map { it.toByte() }.toByteArray()
}