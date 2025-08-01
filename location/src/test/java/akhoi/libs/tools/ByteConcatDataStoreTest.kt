package akhoi.libs.tools

import akhoi.libs.mlct.tools.ByteConcatDataStore
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.random.Random
import kotlin.test.assertContentEquals

class ByteConcatDataStoreTest {

    private lateinit var contentDir: File
    private lateinit var dataStore: ByteConcatDataStore
    private lateinit var tempDir: File

    @Before
    fun setUp() {
        contentDir = File("src/test/resources/akhoi/libs/tools/ByteConcatDataStoreTest")
        tempDir = File("$contentDir/temp")
        tempDir.mkdirs()
    }

    @After
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun testAppend_singleAppend() {
        dataStore = ByteConcatDataStore(tempDir, "testAppend_singleCall")

        val data = byteArrayOf(72, 101, 108, 108, 111, 32, 87, 111, 114, 108, 100)
        dataStore.append(data)

        val actual = File("$tempDir/testAppend_singleCall").readBytes()
        assertContentEquals(data, actual)
    }

    @Test
    fun testAppend_multipleCalls() {
        dataStore = ByteConcatDataStore(tempDir, "testAppend_multipleAppends")
        val data1 = byteArrayOf(72, 101, 108, 108, 111)
        val data2 = byteArrayOf(32, 87, 111, 114, 108, 100)

        dataStore.append(data1)
        dataStore.append(data2)

        val contentFile = File("$tempDir/testAppend_multipleAppends")
        val expected = contentFile.readBytes()
        assertContentEquals(expected, data1 + data2)
    }

    @Test
    fun testRead_wholeFileContent() {
        dataStore = ByteConcatDataStore(contentDir, "test_content")
        val contentFile = File("$contentDir/test_content")
        val actual = dataStore.read(0, contentFile.length().toInt())
        val expected = contentFile.readBytes()
        assertContentEquals(expected, actual)
    }

    @Test
    fun testRead_withOffset() {
        dataStore = ByteConcatDataStore(contentDir, "test_content")
        val actual = dataStore.read(3, 4)
        assertContentEquals("Loca".toByteArray(), actual)
    }

    @Test
    fun testRead_multipleCalls() {
        dataStore = ByteConcatDataStore(contentDir, "test_content")

        var actual = dataStore.read(3, 3)
        assertContentEquals("Loc".toByteArray(), actual)

        actual = dataStore.read(8, 3)
        assertContentEquals("ion".toByteArray(), actual)
    }

    @Test
    fun testRead_beyondContentLength() {
        dataStore = ByteConcatDataStore(contentDir, "test_content")
        val contentFile = File("$contentDir/test_content")
        val actual = dataStore.read(0, (contentFile.length() + 1).toInt())
        assertContentEquals(contentFile.readBytes(), actual)
    }

    @Test
    fun testRead_lastPosition() {
        dataStore = ByteConcatDataStore(contentDir, "test_content")
        val contentLength = File("$contentDir/test_content").length()
        val actual = dataStore.read(contentLength - 1, 1)
        assertContentEquals("n".toByteArray(), actual)
    }

    @Test
    fun testRead_zeroCount() {
        dataStore = ByteConcatDataStore(contentDir, "test_content")
        val actual = dataStore.read(3, 0)
        assertEquals(0, actual.size)
    }

    @Test
    fun testRead_invalidBoundaries() {
        dataStore = ByteConcatDataStore(contentDir, "test_content")
        val contentFile = File("$contentDir/test_content")
        var actual = dataStore.read(0, 0)
        assertContentEquals(ByteArray(0), actual)

        actual = dataStore.read(-1, 3)
        assertContentEquals(ByteArray(0), actual)

        actual = dataStore.read(contentFile.length(), 3)
        assertContentEquals(ByteArray(0), actual)
    }

    @Test
    fun testAppendAndRead_randomData() {
        dataStore = ByteConcatDataStore(tempDir, "testAppendAndRead_randomData")
        val randomData = Random.nextBytes(128)
        dataStore.append(randomData)
        val offset = Random.nextInt(128)
        val count = Random.nextInt((128 - offset))
        val actual = dataStore.read(offset.toLong(), count)
        val expected = randomData.sliceArray(offset..(offset + count - 1))
        assertContentEquals(expected, actual)
    }
}