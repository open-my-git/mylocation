package akhoi.libs.mlct.tools.bcc

import org.junit.Assert
import org.junit.Test
import java.lang.Double
import kotlin.byteArrayOf

class ByteConcatReaderTest {
    @Test
    fun testReadInt_zeroSize() {
        val content = byteArrayOf(0x77)
        val reader = ByteConcatReader(content)
        val actual = reader.readInt(0)
        Assert.assertEquals(0, actual.toLong())
    }

    @Test
    fun testReadInt_partialByte() {
        val content = byteArrayOf(0x77)
        val reader = ByteConcatReader(content)
        val actual = reader.readInt(5)
        Assert.assertEquals(0x0E, actual.toLong())
    }

    @Test
    fun testReadInt_fullByte() {
        val content = byteArrayOf(0x77, 0x33)
        val reader = ByteConcatReader(content)
        val actual = reader.readInt(8)
        Assert.assertEquals(0x77, actual.toLong())
    }

    @Test
    fun testReadInt_oneAndAPartialBytes() {
        val content = byteArrayOf(0x77, 0x33)
        val reader = ByteConcatReader(content)
        val actual = reader.readInt(15)
        Assert.assertEquals(0x3B99, actual.toLong())
    }

    @Test
    fun testReadInt_multipleBytes() {
        val content = byteArrayOf(0x77, 0x33, 0x11)
        val reader = ByteConcatReader(content)
        val actual = reader.readInt(16)
        Assert.assertEquals(0x7733, actual.toLong())
    }

    @Test
    fun testReadInt_fullInteger() {
        val content = byteArrayOf(0x77, 0x33, 0x11, 0x55, 0x00)
        val reader = ByteConcatReader(content)
        val actual = reader.readInt(32)
        Assert.assertEquals(0x77331155, actual.toLong())
    }

    @Test
    fun testReadInt_multipleReads() {
        val content = byteArrayOf(0x77, 0x33)
        val reader = ByteConcatReader(content)
        val firstRead = reader.readInt(5)
        Assert.assertEquals(0x0E, firstRead.toLong())
        val secondRead = reader.readInt(10)
        Assert.assertEquals(0x399, secondRead.toLong())
    }

    @Test
    fun testReset() {
        val content = byteArrayOf(0x77, 0x33)
        val reader = ByteConcatReader(content)
        val firstRead = reader.readInt(5)
        Assert.assertEquals(0x0E, firstRead.toLong())
        reader.reset()
        val secondRead = reader.readInt(15)
        Assert.assertEquals(0x3B99, secondRead.toLong())
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
        Assert.assertEquals(0x2EF10236BD21FC77L, actual)
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
        Assert.assertEquals(0x0BBC408DAF48L, actual)
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
        Assert.assertEquals(0x2EF10236L, actual)
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
        Assert.assertEquals(0x177881L, actual)
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
        Assert.assertEquals(0L, actual)
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
        Assert.assertEquals(0x46D7AL, intNum.toLong())
        Assert.assertEquals(0x43F8EFF4L, longNum)
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
        Assert.assertEquals(0x236BD21L, longNum)
        Assert.assertEquals(0x1F8EFF4, intNum.toLong())
    }

    @Test
    fun testReadInt_exceededContentLength() {
        val reader = ByteConcatReader(
            byteArrayOf(
                0x62, 0x1F, 0xBC.toByte()
            )
        )
        val actual = reader.readInt(32)
        Assert.assertEquals(0x621FBC, actual.toLong())
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
        Assert.assertEquals(0x621FBCFF30ADFC14L, actual)
    }

    @Test
    fun testReadDouble() {
        val reader = ByteConcatReader(
            byteArrayOf(
                0x23, 0xFE.toByte(), 0xB7.toByte(), 0x09,
                0x12, 0x98.toByte(), 0xF1.toByte(), 0xDC.toByte()
            )
        )
        val actual = reader.readDouble(64)
        Assert.assertEquals(0x23FEB7091298F1DCL, Double.doubleToRawLongBits(actual))
    }

    @Test
    fun testSkip() {
        val reader = ByteConcatReader(byteArrayOf(0x23, 0xFE.toByte()))
        val firstRead = reader.readInt(4)
        reader.skip(3)
        val secondRead = reader.readLong(2)
        Assert.assertEquals(0x2, firstRead.toLong())
        Assert.assertEquals(0x3, secondRead)
    }
}
