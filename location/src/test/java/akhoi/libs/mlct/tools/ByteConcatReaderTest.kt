package akhoi.libs.mlct.tools

import kotlin.test.Test
import kotlin.test.assertEquals

class ByteConcatReaderTest {
    @Test
    fun testReadInt_zeroSize() {
        val content = byteArrayOf(0x77)
        val reader = ByteConcatReader(content)
        val actual = reader.readInt(0)
        assertEquals(0, actual)
    }

    @Test
    fun testReadInt_partialByte() {
        val content = byteArrayOf(0x77)
        val reader = ByteConcatReader(content)
        val actual = reader.readInt(5)
        assertEquals(0xE, actual)
    }

    @Test
    fun testReadInt_fullByte() {
        val content = byteArrayOf(0x77, 0x33)
        val reader = ByteConcatReader(content)
        val actual = reader.readInt(8)
        assertEquals(0x77, actual)
    }

    @Test
    fun testReadInt_oneAndAPartialBytes() {
        val content = byteArrayOf(0x77, 0x33)
        val reader = ByteConcatReader(content)
        val actual = reader.readInt(15)
        assertEquals(0x3B99, actual)
    }

    @Test
    fun testReadInt_multipleBytes() {
        val content = byteArrayOf(0x77, 0x33, 0x11)
        val reader = ByteConcatReader(content)
        val actual = reader.readInt(16)
        assertEquals(0x7733, actual)
    }

    @Test
    fun testReadInt_fullInteger() {
        val content = byteArrayOf(0x77, 0x33, 0x11, 0x55, 0x00)
        val reader = ByteConcatReader(content)
        val actual = reader.readInt(32)
        assertEquals(0x77331155, actual)
    }

    @Test
    fun testReadInt_multipleReads() {
        val content = byteArrayOf(0x77, 0x33)
        val reader = ByteConcatReader(content)
        val firstRead = reader.readInt(5)
        assertEquals(0xE, firstRead)
        val secondRead = reader.readInt(10)
        assertEquals(0x399, secondRead)
    }

    @Test
    fun testReset() {
        val content = byteArrayOf(0x77, 0x33)
        val reader = ByteConcatReader(content)
        val firstRead = reader.readInt(5)
        assertEquals(0xE, firstRead)
        reader.reset()
        val secondRead = reader.readInt(15)
        assertEquals(0x3B99, secondRead)
    }

    @Test
    fun testReadLong_full() {
        val reader = ByteConcatReader(
            byteArrayOf(
                0x2E, 0xF1.toByte(), 0x02, 0x36,
                0xBD.toByte(), 0x21, 0xFC.toByte(), 0x77
            )
        )
        val actual = reader.readLong(64)
        assertEquals(0x2EF10236BD21FC77, actual)
    }

    @Test
    fun testReadLong_partial() {
        val reader = ByteConcatReader(
            byteArrayOf(
                0x2E, 0xF1.toByte(), 0x02, 0x36,
                0xBD.toByte(), 0x21, 0xFC.toByte(), 0x77
            )
        )
        val actual = reader.readLong(46)
        assertEquals(0xBBC408DAF48, actual)
    }

    @Test
    fun testReadLong_integerSize() {
        val reader = ByteConcatReader(
            byteArrayOf(
                0x2E, 0xF1.toByte(), 0x02, 0x36,
                0xBD.toByte(), 0x21, 0xFC.toByte(), 0x77
            )
        )
        val actual = reader.readLong(32)
        assertEquals(0x2EF10236, actual)
    }

    @Test
    fun testReadLong_partialIntegerSize() {
        val reader = ByteConcatReader(
            byteArrayOf(
                0x2E, 0xF1.toByte(), 0x02, 0x36,
                0xBD.toByte(), 0x21, 0xFC.toByte(), 0x77
            )
        )
        val actual = reader.readLong(23)
        assertEquals(0x177881, actual)
    }

    @Test
    fun testReadLong_zeroSize() {
        val reader = ByteConcatReader(
            byteArrayOf(
                0x2E, 0xF1.toByte(), 0x02, 0x36,
                0xBD.toByte(), 0x21, 0xFC.toByte(), 0x77
            )
        )
        val actual = reader.readLong(0)
        assertEquals(0, actual)
    }

    @Test
    fun testReadLong_afterReadInt() {
        val reader = ByteConcatReader(
            byteArrayOf(
                0x02, 0x36, 0xBD.toByte(), 0x21,
                0xFC.toByte(), 0x77, 0xFA.toByte(), 0x16
            )
        )
        val intNum = reader.readInt(25)
        val longNum = reader.readLong(32)
        assertEquals(0x46D7A, intNum)
        assertEquals(0x43F8EFF4, longNum)
    }

    @Test
    fun testReadInt_afterReadLong() {
        val reader = ByteConcatReader(
            byteArrayOf(
                0x02, 0x36, 0xBD.toByte(), 0x21,
                0xFC.toByte(), 0x77, 0xFA.toByte(), 0x16
            )
        )
        val longNum = reader.readLong(32)
        val intNum = reader.readInt(25)
        assertEquals(0x236BD21, longNum)
        assertEquals(0x1F8EFF4, intNum)
    }

    @Test
    fun testReadInt_exceededContentLength() {
        val reader = ByteConcatReader(
            byteArrayOf(
                0x62, 0x1F, 0xBC.toByte()
            )
        )
        val actual = reader.readInt(32)
        assertEquals(0x621FBC, actual)
    }

    @Test
    fun testReadLong_exceededNumberSize() {
        val reader = ByteConcatReader(
            byteArrayOf(
                0x62, 0x1F, 0xBC.toByte(), 0xFF.toByte(),
                0x30, 0xAD.toByte(), 0xFC.toByte(), 0x14,
                0x44, 0x95.toByte()
            )
        )
        val actual = reader.readLong(65)
        assertEquals(0x621FBCFF30ADFC14, actual)
    }

    @Test
    fun testReadDouble() {
        val reader = ByteConcatReader(
            byteArrayOf(
                0x23, 0xFE.toByte(), 0xB7.toByte(), 0x09,
                0x12, 0x98.toByte(), 0xF1.toByte(), 0xDC.toByte(),
            )
        )
        val actual = reader.readDouble(64)
        assertEquals(0x23FEB7091298F1DC, java.lang.Double.doubleToRawLongBits(actual))
    }

    @Test
    fun testSkip() {
        val reader = ByteConcatReader(byteArrayOf(0x23, 0xFE.toByte()))
        val firstRead = reader.readInt(4)
        reader.skip(3)
        val secondRead = reader.readLong(2)
        assertEquals(0x2, firstRead)
        assertEquals(0x3, secondRead)
    }
}