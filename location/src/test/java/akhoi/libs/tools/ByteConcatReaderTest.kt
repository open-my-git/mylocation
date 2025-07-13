package akhoi.libs.tools

import kotlin.test.Test
import kotlin.test.assertEquals

class ByteConcatReaderTest {
    @Test
    fun testReadInt_partialByte() {
        val content = byteArrayFromInts(0x77)
        val reader = ByteConcatReader(content)
        val actual = reader.readInt(5)
        assertEquals(14, actual)
    }

    @Test
    fun testReadInt_fullByte() {
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

    private fun byteArrayFromInts(vararg ints: Int) = ints.map { it.toByte() }.toByteArray()
}