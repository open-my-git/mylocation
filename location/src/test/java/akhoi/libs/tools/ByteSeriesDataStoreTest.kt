package akhoi.libs.tools

import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.random.Random

class ByteSeriesDataStoreTest {

    private lateinit var tempDir: File
    private lateinit var dataStore: ByteSeriesDataStore

    @Before
    fun setUp() {
        val tempDirPath = createTempDirectory()
        tempDir = File(tempDirPath.toString())

        dataStore = ByteSeriesDataStore(tempDir, "test_store")
        dataStore.initialize()
    }

    @After
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun test_appendAndReadAll() {
        val data = byteArrayOf(72, 101, 108, 108, 111, 32, 87, 111, 114, 108, 100)
        dataStore.append(data)

        val actual = dataStore.read(0, data.size)
        assertArrayEquals(data, actual)
    }

    @Test
    fun testAppendAndReadTheMiddlePart() {
        val data = byteArrayOf(72, 101, 108, 108, 111, 32, 87, 111, 114, 108, 100)
        dataStore.append(data)

        val actual = dataStore.read(3, 4)
        assertArrayEquals(byteArrayOf(108, 111, 32, 87), actual)
    }

    @Test
    fun test_appendTwice_readAllAtOnce() {
        val data1 = byteArrayOf(72, 101, 108, 108, 111)
        val data2 = byteArrayOf(32, 87, 111, 114, 108, 100)

        dataStore.append(data1)
        dataStore.append(data2)

        val actual = dataStore.read(0, data1.size + data2.size)
        assertArrayEquals(byteArrayOf(72, 101, 108, 108, 111, 32, 87, 111, 114, 108, 100), actual)
    }

    @Test
    fun test_appendTwice_readTwice() {
        val data1 = byteArrayOf(72, 101, 108, 108, 111)
        val data2 = byteArrayOf(32, 87, 111, 114, 108, 100)

        dataStore.append(data1)
        dataStore.append(data2)

        var result = dataStore.read(3, 3)
        assertArrayEquals(byteArrayOf(108, 111, 32), result)

        result = dataStore.read(8, 3)
        assertArrayEquals(byteArrayOf(114, 108, 100), result)
    }

    @Test
    fun test_randomData() {
        val randomData = Random.nextBytes(128)
        dataStore.append(randomData)
        val offset = Random.nextInt(128)
        val limit = Random.nextInt((128 - offset))
        val actual = dataStore.read(offset.toLong(), limit)
        val expected = randomData.sliceArray(offset..(offset + limit - 1))
        assertArrayEquals(expected, actual)
    }

    @Test
    fun test_offsetLargerThanContent() {
        val data = byteArrayOf(72, 101, 108, 108, 111)
        dataStore.append(data)

        val actual = dataStore.read(10, 5)
        assertEquals(0, actual.size)
    }

    @Test
    fun test_negativeOffset() {
        val data = byteArrayOf(72, 101, 108, 108, 111)
        dataStore.append(data)

        val result1 = dataStore.read(-1, 5)
        assertEquals(0, result1.size)
    }

    @Test
    fun test_offsetEqualsToContentLength() {
        val data = byteArrayOf(72, 101, 108, 108, 111)
        dataStore.append(data)

        val result1 = dataStore.read(6, 5)
        assertEquals(0, result1.size)
    }

    @Test
    fun test_zeroLimit() {
        val data = byteArrayOf(72, 101, 108, 108, 111)
        dataStore.append(data)

        val result1 = dataStore.read(3, 0)
        assertEquals(0, result1.size)
    }

    @Test
    fun test_negativeLimit() {
        val data = byteArrayOf(72, 101, 108, 108, 111)
        dataStore.append(data)

        val result1 = dataStore.read(0, -3)
        assertEquals(0, result1.size)
    }

    @Test
    fun test_limitLargerThanAllowed() {
        dataStore.maxLimit = 10
        val tooLargeLimit = 20
        val data = ByteArray(tooLargeLimit)
        dataStore.append(data)

        val actual = dataStore.read(0, tooLargeLimit)
        assertEquals(dataStore.maxLimit, actual.size)
        assertArrayEquals(data.sliceArray(0..dataStore.maxLimit - 1), actual)
    }
}