package akhoi.libs.tools

import kotlin.test.Test
import kotlin.test.assertEquals

class ByteConcatReaderTest {
    @Test
    fun testReadInt_zeroSize() {
        val content = byteArrayFromInts(0x77)
        val reader = ByteConcatReader(content)
        val actual = reader.readInt(0)
        assertEquals(0, actual)
    }

    @Test
    fun testReadInt_subByte() {
        val content = byteArrayFromInts(0x77)
        val reader = ByteConcatReader(content)
        val actual = reader.readInt(5)
        assertEquals(14, actual)
    }

    @Test
    fun testReadInt_completeByte() {
        val content = byteArrayFromInts(0x77, 0x33)
        val reader = ByteConcatReader(content)
        val actual = reader.readInt(8)
        assertEquals(0x77, actual)
    }

    @Test
    fun testReadInt_multipleBytes() {
        val content = byteArrayFromInts(0x77, 0x33)
        val reader = ByteConcatReader(content)
        val actual = reader.readInt(15)
        assertEquals(15257, actual)
    }

    @Test
    fun testReadInt_multipleReads() {
        val content = byteArrayFromInts(0x77, 0x33)
        val reader = ByteConcatReader(content)
        val firstRead = reader.readInt(5)
        assertEquals(14, firstRead)
        val secondRead = reader.readInt(10)
        assertEquals(921, secondRead)
    }

    @Test
    fun testReset() {
        val content = byteArrayFromInts(0x77, 0x33)
        val reader = ByteConcatReader(content)
        val firstRead = reader.readInt(5)
        assertEquals(14, firstRead)
        reader.reset()
        val secondRead = reader.readInt(15)
        assertEquals(15257, secondRead)
    }

    @Test
    fun testReadLong_longSize() {
        val reader = ByteConcatReader(
            byteArrayFromInts(
                0x2E, 0xF1, 0x02, 0x36,
                0xBD, 0x21, 0xFC, 0x77
            )
        )
        val actual = reader.readLong(64)
        assertEquals(0x2EF10236BD21FC77, actual)
    }

    @Test
    fun testReadLong_subLongSize() {
        val reader = ByteConcatReader(
            byteArrayFromInts(
                0x2E, 0xF1, 0x02, 0x36,
                0xBD, 0x21, 0xFC, 0x77
            )
        )
        val actual = reader.readLong(46)
        assertEquals(0xBBC408DAF48, actual)
    }

    @Test
    fun testReadLong_integerSize() {
        val reader = ByteConcatReader(
            byteArrayFromInts(
                0x2E, 0xF1, 0x02, 0x36,
                0xBD, 0x21, 0xFC, 0x77
            )
        )
        val actual = reader.readLong(32)
        assertEquals(0x2EF10236, actual)
    }

    @Test
    fun testReadLong_subIntegerSize() {
        val reader = ByteConcatReader(
            byteArrayFromInts(
                0x2E, 0xF1, 0x02, 0x36,
                0xBD, 0x21, 0xFC, 0x77
            )
        )
        val actual = reader.readLong(23)
        assertEquals(0x177881, actual)
    }

    @Test
    fun testReadLong_zeroSize() {
        val reader = ByteConcatReader(
            byteArrayFromInts(
                0x2E, 0xF1, 0x02, 0x36,
                0xBD, 0x21, 0xFC, 0x77
            )
        )
        val actual = reader.readLong(0)
        assertEquals(0, actual)
    }

    private fun byteArrayFromInts(vararg ints: Int) = ints.map { it.toByte() }.toByteArray()
}