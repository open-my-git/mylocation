package akhoi.libs.mlct.tools

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.createTempDirectory
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class FileNamePropertiesTest {
    private lateinit var fileNameProperties: FileNameProperties
    private lateinit var rootDir: File
    private lateinit var testScope: TestScope
    private lateinit var ioScope: CoroutineScope
    private lateinit var mockFileWatcher: FileWatcher

    @Before
    fun setup() {
        val tempDirPath = createTempDirectory()
        rootDir = tempDirPath.toFile()
        testScope = TestScope()
        ioScope = CoroutineScope(Dispatchers.IO)
        mockFileWatcher = mock()
        fileNameProperties = FileNameProperties(rootDir, mockFileWatcher)
    }

    @After
    fun tearDown() {
        fileNameProperties.clear()
        rootDir.deleteRecursively()
    }

    @Test
    fun testLong() {
        fileNameProperties["long_key"] = 12912840L
        assertEquals(12912840L, fileNameProperties["long_key"])
    }

    @Test
    fun testLong_random() {
        val randomLong = Random(System.currentTimeMillis()).nextLong()
        fileNameProperties["long_key"] = randomLong
        assertEquals(randomLong.toString(), fileNameProperties["long_key"], "Random long: $randomLong")
    }

    @Test
    fun testByte() {
        fileNameProperties["byte_key"] = 123.toByte()
        assertEquals(123.toByte(), fileNameProperties["byte_key"])
    }

    @Test
    fun testByte_random() {
        val randomByte = Random(System.currentTimeMillis())
            .nextInt(Byte.MIN_VALUE.toInt(), Byte.MAX_VALUE.toInt() + 1)
            .toByte()
        fileNameProperties["byte_key"] = randomByte
        assertEquals(randomByte.toString(), fileNameProperties["byte_key"], "Random byte: $randomByte")
    }

    @Test
    fun testUByte() {
        fileNameProperties["ubyte_key"] = 200.toUByte()
        assertEquals(200.toUByte(), fileNameProperties["ubyte_key"])
    }

    @Test
    fun testUByte_random() {
        val randomUByte = Random(System.currentTimeMillis())
            .nextInt(0, UByte.MAX_VALUE.toInt() + 1)
            .toUByte()
        fileNameProperties["ubyte_key"] = randomUByte
        assertEquals(randomUByte.toString(), fileNameProperties["ubyte_key"], "Random ubyte: $randomUByte")
    }

    @Test
    fun testShort() {
        fileNameProperties["short_key"] = 12345.toShort()
        assertEquals(12345.toShort(), fileNameProperties["short_key"])
    }

    @Test
    fun testShort_random() {
        val randomShort = Random(System.currentTimeMillis())
            .nextInt(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt() + 1)
            .toShort()
        fileNameProperties["short_key"] = randomShort
        assertEquals(randomShort.toString(), fileNameProperties["short_key"], "Random short: $randomShort")
    }

    @Test
    fun testUShort() {
        fileNameProperties["ushort_key"] = 40000.toUShort()
        assertEquals(40000.toUShort(), fileNameProperties["ushort_key"])
    }

    @Test
    fun testUShort_random() {
        val randomUShort = Random(System.currentTimeMillis())
            .nextInt(0, UShort.MAX_VALUE.toInt() + 1)
            .toUShort()
        fileNameProperties["ushort_key"] = randomUShort
        assertEquals(randomUShort.toString(), fileNameProperties["ushort_key"], "Random ushort: $randomUShort")
    }

    @Test
    fun testInt() {
        fileNameProperties["int_key"] = 123456789
        assertEquals(123456789, fileNameProperties["int_key"])
    }

    @Test
    fun testInt_random() {
        val randomInt = Random(System.currentTimeMillis()).nextInt()
        fileNameProperties["int_key"] = randomInt
        assertEquals(randomInt.toString(), fileNameProperties["int_key"], "Random int: $randomInt")
    }

    @Test
    fun testUInt() {
        fileNameProperties["uint_key"] = 3000000000U
        assertEquals(3000000000U, fileNameProperties["uint_key"])
    }

    @Test
    fun testUInt_random() {
        val randomUInt = Random(System.currentTimeMillis()).nextInt().toUInt()
        fileNameProperties["uint_key"] = randomUInt
        assertEquals(randomUInt.toString(), fileNameProperties["uint_key"], "Random uint: $randomUInt")
    }

    @Test
    fun testULong() {
        fileNameProperties["ulong_key"] = 9000000000000000000UL
        assertEquals(9000000000000000000UL, fileNameProperties["ulong_key"])
    }

    @Test
    fun testULong_random() {
        val randomULong = Random(System.currentTimeMillis()).nextLong().toULong()
        fileNameProperties["ulong_key"] = randomULong
        assertEquals(randomULong.toString(), fileNameProperties["ulong_key"], "Random ulong: $randomULong")
    }

    @Test
    fun testFloat() {
        fileNameProperties["float_key"] = 123.456f
        assertEquals(123.456f, fileNameProperties["float_key"])
    }

    @Test
    fun testFloat_random() {
        val randomFloat = Random(System.currentTimeMillis()).nextFloat()
        fileNameProperties["float_key"] = randomFloat
        assertEquals(randomFloat.toString(), fileNameProperties["float_key"], "Random float: $randomFloat")
    }

    @Test
    fun testDouble() {
        fileNameProperties["double_key"] = 123456.789
        assertEquals(123456.789, fileNameProperties["double_key"])
    }

    @Test
    fun testDouble_random() {
        val randomDouble = Random(System.currentTimeMillis()).nextDouble()
        fileNameProperties["double_key"] = randomDouble
        assertEquals(randomDouble.toString(), fileNameProperties["double_key"], "Random double: $randomDouble")
    }

    @Test
    fun testLong_null() {
        fileNameProperties["long_key"] = null
        assertNull(fileNameProperties["long_key"])
    }

    @Test
    fun testByte_null() {
        fileNameProperties["byte_key"] = null
        assertNull(fileNameProperties["byte_key"])
    }

    @Test
    fun testUByte_null() {
        fileNameProperties["ubyte_key"] = null
        assertNull(fileNameProperties["ubyte_key"])
    }

    @Test
    fun testShort_null() {
        fileNameProperties["short_key"] = null
        assertNull(fileNameProperties["short_key"])
    }

    @Test
    fun testUShort_null() {
        fileNameProperties["ushort_key"] = null
        assertNull(fileNameProperties["ushort_key"])
    }

    @Test
    fun testInt_null() {
        fileNameProperties["int_key"] = null
        assertNull(fileNameProperties["int_key"])
    }

    @Test
    fun testUInt_null() {
        fileNameProperties["uint_key"] = null
        assertNull(fileNameProperties["uint_key"])
    }

    @Test
    fun testULong_null() {
        fileNameProperties["ulong_key"] = null
        assertNull(fileNameProperties["ulong_key"])
    }

    @Test
    fun testFloat_null() {
        fileNameProperties["float_key"] = null
        assertNull(fileNameProperties["float_key"])
    }

    @Test
    fun testDouble_null() {
        fileNameProperties["double_key"] = null
        assertNull(fileNameProperties["double_key"])
    }

    @Test
    fun testSet_String() {
        fileNameProperties["string_key"] = "string_value"
        assertEquals("string_value", readValue("string_key"))
    }

    @Test
    fun testGet_String() {
        fileNameProperties["string_key"] = "string_value"
        assertEquals("string_value", fileNameProperties["string_key"])
    }

    @Test
    fun testSet_String_empty() {
        fileNameProperties["string_key"] = ""
        assertEquals("", fileNameProperties["string_key"])
    }

    @Test
    fun testSet_String_null() {
        fileNameProperties["string_key"] = null
        assertEquals(null, readValue("string_key"))
    }

    @Test
    fun testSet_String_random_specialChars() {
        val charPool = "!@#$%^&*()_+-=[]{}|;:,.<>?" + (0..31).map(::Char) + Char(127)
        val stringValue = (1..10).map { charPool.random() }.joinToString("")
        fileNameProperties["string_key"] = stringValue
        assertEquals(stringValue, readValue("string_key"), "Random special string: $stringValue")
    }

    @Test
    fun testString_random_printableChars() {
        val charPool = (32..126).map(::Char)
        val randomString = (1..100).map { charPool.random() }.joinToString("")
        fileNameProperties["random_string_key"] = randomString
        assertEquals(randomString, fileNameProperties["random_string_key"], "Random printable string: $randomString")
    }

    @Test
    fun testSet_String_overwrite() {
        fileNameProperties["string_key"] = "first_string_value"
        assertEquals("first_string_value", readValue("string_key"))
        fileNameProperties["string_key"] = "second_string_value"
        assertEquals("second_string_value", readValue("string_key"))
    }

    @Test
    fun testRemove() {
        assertNull(readValue("test_key"))
        fileNameProperties["test_key"] = "test_value"
        assertEquals("test_value", readValue("test_key"))
        fileNameProperties.remove("test_key")
        assertEquals(null, readValue("test_key"))
    }

    @Test
    fun testContains() {
        assertFalse(fileNameProperties.contains("test_key"))
        fileNameProperties["test_key"] = "test_value"
        assertTrue(fileNameProperties.contains("test_key"))
    }

    @Test
    fun testString_valueLengthAboveLimit() {
        val value = (0..FileNameProperties.MAX_VALUE_LEN).joinToString("") { "i" }
        fileNameProperties["test_key"] = value
        assertEquals(value, fileNameProperties["test_key"])
    }

    @Test
    fun testFlowGet_createdEvent() = runTest {
        whenever(mockFileWatcher.watchFile("test_key"))
            .thenReturn(flowOf(
                FileWatcher.EventData(FileWatcher.EventType.CREATED, Path("test_value"))
            ))
        val flow = fileNameProperties.flowGet("test_key", String::class)
        val value = flow.first()
        assertEquals("test_value", value)
    }

    @Test
    fun testFlowGet_updatedEvent() = runTest {
        whenever(mockFileWatcher.watchFile("test_key"))
            .thenReturn(flowOf(
                FileWatcher.EventData(FileWatcher.EventType.UPDATED, Path("test_value"))
            ))
        val flow = fileNameProperties.flowGet("test_key", String::class)
        val value = flow.first()
        assertEquals("test_value", value)
    }

    @Test
    fun testFlowGet_deletedEvent() = runTest {
        whenever(mockFileWatcher.watchFile("test_key"))
            .thenReturn(flowOf(
                FileWatcher.EventData(FileWatcher.EventType.DELETED, Path("test_value"))
            ))
        val flow = fileNameProperties.flowGet("test_key", String::class)
        val value = flow.first()
        assertEquals(null, value)
    }

    @Test(NoSuchElementException::class)
    fun testFlowGet_unexistedKeys() = runTest {
        whenever(mockFileWatcher.watchFile("test_key"))
            .thenReturn(flowOf(
                FileWatcher.EventData(FileWatcher.EventType.CREATED, Path("test_value"))
            ))
        whenever(mockFileWatcher.watchFile("unexisted_test_key"))
            .thenReturn(flowOf())
        val flow = fileNameProperties.flowGet("unexisted_test_key", String::class)
        flow.first() // this line never reaches
    }

    private fun readValue(key: String): String? {
        val keyDir = File("$rootDir/$key")
        if (!keyDir.exists()) {
            return null
        }
        val keyFile = keyDir.listFiles()?.firstOrNull()
            ?: return ""
        if (keyFile.length() > 0L) {
            return keyFile.readText()
        }
        return keyFile.name
    }
}