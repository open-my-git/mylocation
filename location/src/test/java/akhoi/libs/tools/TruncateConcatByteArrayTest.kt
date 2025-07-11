package akhoi.libs.tools

import org.junit.Test
import kotlin.math.min
import kotlin.test.assertContentEquals

class TruncateConcatByteArrayTest {
    @Test
    fun testAppendLong_singleByteNonTruncated_positive_manualAssert() {
        val byteConcat = TruncateConcatByteArray(8)
        byteConcat.appendLong(61097752, 64)

        val expected = byteArrayOf(
            0x00, 0x00, 0x00, 0x00, 0x03, 0xA4.toByte(), 0x47.toByte(), 0x18
        )

        assertContentEquals(expected, byteConcat.content)
    }

    @Test
    fun testAppendLong_singleByteNonTruncated_negative_manualAssert() {
        val byteConcat = TruncateConcatByteArray(8)
        byteConcat.appendLong(-61097752, 64)

        val expected = intArrayOf(
            0xFF, 0xFF, 0xFF, 0xFF, 0xFC, 0x5B, 0xB8, 0xE8
        ).map { it.toByte() }.toByteArray()

        assertContentEquals(expected, byteConcat.content)
    }

    @Test
    fun testAppendLong_multipleBytesNonTruncated_programmedAssert() {
        val testCases = listOf(
            6154668049787298341L,
            -6154668049787298341L,
            3472815634280521738L,
            -3472815634280521738L,
            2594839102756397075L,
            -2594839102756397075L,
            7932485601273948165L,
            -7932485601273948165L,
            4826593017452863491L,
            -4826593017452863491L
        )

        testCases.forEach { value ->
            val byteConcat = TruncateConcatByteArray(8)
            byteConcat.appendLong(value, 64)

            val expected = ByteArray(8)
            for (i in 0..7) {
                val byteIndex = 8 * (7 - i)
                expected[i] = ((value shl (56 - byteIndex)) shr 56).toByte()
            }

            assertContentEquals(expected, byteConcat.content)
        }
    }

    @Test
    fun testAppendLong_multipleBytesTruncated_withAlignedBits() {
        val byteConcat = TruncateConcatByteArray(6)
        byteConcat.appendLong(384, 8)
        byteConcat.appendLong(98304, 16)
        byteConcat.appendLong(25165824, 24)

        val expected = byteArrayOf(
            0x80.toByte(), 0x80.toByte(), 0x00, 0x80.toByte(), 0x00, 0x00
        )

        assertContentEquals(expected, byteConcat.content)
    }

    @Test
    fun testAppendLong_multipleBytesTruncated_withUnalignedBits() {
        val byteConcat = TruncateConcatByteArray(13)
        byteConcat.appendLong(8556177825808674934, 17)
        byteConcat.appendLong(5036613889682970945, 35)
        byteConcat.appendLong(3794196406690437216, 51)

        val expected = intArrayOf(
            0x38, 0x3B, 0x5C, 0x55, 0xA1, 0x94, 0x1F, 0x63, 0x0F, 0x09, 0xB1, 0xB0, 0xC0
        ).map { it.toByte() }.toByteArray()

        assertContentEquals(expected, byteConcat.content)
    }

    @Test
    fun testAppendLong_singleByteTruncated() {
        val byteConcat = TruncateConcatByteArray(1)
        byteConcat.appendLong(24, 4)

        val expected = byteArrayOf(0x80.toByte())
        assertContentEquals(expected, byteConcat.content)
    }

    @Test
    fun testAppend_LongAndDouble_programmedAssert() {
        val byteConcat = TruncateConcatByteArray(23)

        val long1 = 1L
        val long2 = 3572460L
        val double1 = 3777.49
        val double2 = -1.224194
        val long3 = 51788L
        val double3 = 4300.0

        byteConcat.appendLong(long1, 7)
        byteConcat.appendLong(long2, 13)
        byteConcat.appendDouble(double1, 39)
        byteConcat.appendDouble(double2, 55)
        byteConcat.appendLong(long3, 64)
        byteConcat.appendDouble(double3, 1)

        val binaryString = getBinaryString(long1, 7) +
                getBinaryString(long2, 13) +
                getBinaryString(double1, 39) +
                getBinaryString(double2, 55) +
                getBinaryString(long3, 64) +
                getBinaryString(double3, 1)

        val expected = binaryStringToByteArray(binaryString, 23)

        assertContentEquals(expected, byteConcat.content)
    }

    @Test
    fun testAppend_LongAndDouble_manualAssert() {
        val byteConcat = TruncateConcatByteArray(19)

        byteConcat.appendLong(1L, 4)
        byteConcat.appendLong(3572460L, 27)
        byteConcat.appendDouble(37.7749, 39)
        byteConcat.appendDouble(-122.4194, 39)
        byteConcat.appendDouble(43.0, 39)

        val expected = intArrayOf(
            0x10, 0x6d, 0x05, 0xd8, 0xbf, 0xb1, 0x5b, 0x57,
            0x42, 0xbb, 0x98, 0xc7, 0xe2, 0x80, 0x00, 0x00,
            0x00, 0x00, 0x00
        ).map { it.toByte() }.toByteArray()
        assertContentEquals(expected, byteConcat.content)
    }

    @Test
    fun testAppendLong_zeroValueSize() {
        val byteConcat = TruncateConcatByteArray(3)
        byteConcat.appendLong(12730100, 0)
        val expected = byteArrayOf(0, 0, 0)
        assertContentEquals(expected, byteConcat.content)
    }

    @Test
    fun testAppendDouble_zeroValueSize() {
        val byteConcat = TruncateConcatByteArray(5)
        byteConcat.appendDouble(11431250.019303, 0)
        val expected = byteArrayOf(0, 0, 0, 0, 0)
        assertContentEquals(expected, byteConcat.content)
    }

    @Test
    fun testAppendLong_maxValueSize() {
        val byteConcat = TruncateConcatByteArray(8)
        byteConcat.appendLong(1412317098, 64)
        val expected = byteArrayOf(
            0, 0, 0, 0, 0x54, 0x2E, 0x3F, 0xAA.toByte()
        )
        assertContentEquals(expected, byteConcat.content)
    }

    @Test
    fun testAppendDouble_maxValueSize() {
        val byteConcat = TruncateConcatByteArray(8)
        byteConcat.appendDouble(15235.918, 64)
        val expected = byteArrayOf(
            0x40, 0xcd.toByte(), 0xc1.toByte(), 0xf5.toByte(),
            0x81.toByte(), 0x06, 0x24, 0xdd.toByte()
        )
        assertContentEquals(expected, byteConcat.content)
    }

    @Test
    fun testAppendLong_notEnoughCapacity() {
        val byteConcat = TruncateConcatByteArray(3)
        byteConcat.appendLong(572662306, 32)
        val expected = byteArrayOf(0x22, 0x22, 0x22)
        assertContentEquals(expected, byteConcat.content)
    }

    @Test
    fun testAppendDouble_notEnoughCapacity() {
        val byteConcat = TruncateConcatByteArray(2)
        byteConcat.appendDouble(3450283.6028, 19)
        val expected = byteArrayOf(0x11, 0x9C.toByte())
        assertContentEquals(expected, byteConcat.content)
    }

    private fun getBinaryString(value: Long, valueSize: Int): String {
        return java.lang.Long.toBinaryString(value)
            .padStart(Long.SIZE_BITS, '0')
            .substring(Long.SIZE_BITS - valueSize, Long.SIZE_BITS)
    }

    private fun getBinaryString(value: Double, valueSize: Int): String {
        return getBinaryString(java.lang.Double.doubleToRawLongBits(value), valueSize)
    }

    private fun binaryStringToByteArray(binaryString: String, arraySize: Int): ByteArray {
        val expected = ByteArray(arraySize)
        for (i in 0..(arraySize - 1) * 8 step 8) {
            val byteString =
                binaryString.substring(i, min(i + 8, binaryString.length)).padEnd(8, '0')
            expected[i / 8] = byteString.toInt(2).toByte()
        }
        return expected
    }
}