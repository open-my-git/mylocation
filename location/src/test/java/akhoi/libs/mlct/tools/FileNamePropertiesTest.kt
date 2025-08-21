package akhoi.libs.mlct.tools

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.mockito.kotlin.mock
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FileNamePropertiesTest {
    lateinit var fileNameProperties: FileNameProperties
    lateinit var tempDir: File
    lateinit var testScope: TestScope
    lateinit var ioScope: CoroutineScope

    @Before
    fun setup() {
        val tempDirPath = createTempDirectory()
        tempDir = File(tempDirPath.toString())
        testScope = TestScope()
        ioScope = CoroutineScope(Dispatchers.IO)
        fileNameProperties = FileNameProperties(tempDir)
    }

    @After
    fun tearDown() {
        fileNameProperties.clear()
        tempDir.deleteRecursively()
    }

    @Test
    fun testLong_normal() {
        fileNameProperties["long_key"] = 12912840L
        assertEquals(12912840L, fileNameProperties.get<Long>("long_key"))
    }

    @Test
    fun testLong_zero() {
        fileNameProperties["long_key"] = 0L
        assertEquals(0L, fileNameProperties.get("long_key"))
    }

    @Test
    fun testLong_random() {
        val randomLong = Random(System.currentTimeMillis()).nextLong()
        fileNameProperties["random_long_key"] = randomLong
        assertEquals(randomLong, fileNameProperties["random_long_key"])
    }

    @Test
    fun testLong_remove() {
        fileNameProperties["long_key"] = 100L
        assertEquals(100L, fileNameProperties.get("long_key"))
        fileNameProperties.remove("long_key")
        assertEquals(null as Long?, fileNameProperties.get("long_key"))
    }

    @Test
    fun testString_empty() {
        fileNameProperties["string_key"] = ""
        assertEquals("", fileNameProperties.get("string_key"))
    }

    @Test
    fun testString_random() {
        val chars =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+-=[]{}|;:,.<>?"
        val randomString = (1..100).map { chars.random() }.joinToString("")
        fileNameProperties["random_string_key"] = randomString
        assertEquals(randomString, fileNameProperties.get("random_string_key"))
    }

    @Test
    fun testString_remove() {
        fileNameProperties["string_key"] = "string_value"
        assertEquals("string_value", fileNameProperties.get("string_key"))
        fileNameProperties.remove("string_key")
        assertEquals(null, fileNameProperties.get<String>("string_key"))
    }

    @Test
    fun testString_valueOverwritten() {
        fileNameProperties["string_key"] = "string_value_first"
        assertEquals("string_value_first", fileNameProperties.get("string_key"))
        fileNameProperties["string_key"] = "string_value_second"
        assertEquals("string_value_second", fileNameProperties.get<String>("string_key"))
    }

    @Test
    fun testGet_withoutPut() {
        assertEquals(null, fileNameProperties.get<Long>("key"))
    }

    @Test
    fun testRemove_withPut() {
        assertFalse(fileNameProperties.contains("test_key"))
        fileNameProperties["test_key"] = "test_value"
        assertTrue(fileNameProperties.contains("test_key"))
        fileNameProperties.remove("test_key")
        assertFalse(fileNameProperties.contains("test_key"))
    }

    @Test
    fun testRemove_withoutPut() {
        assertFalse(fileNameProperties.contains("nonexistent_key"))
        fileNameProperties.remove("nonexistent_key")
        assertFalse(fileNameProperties.contains("nonexistent_key"))
    }

    @Test
    fun testContains() {
        assertFalse(fileNameProperties.contains("to_be_added_key"))
        fileNameProperties["to_be_added_key"] = "to_be_added_value"
        assertTrue(fileNameProperties.contains("to_be_added_key"))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testGetFlow() = runTest {
        val flow = fileNameProperties.flowGet("test_key", String::class)
        var items = mutableListOf<String?>()
        testScope.launch {
            items = flow.flowOn(testScheduler)
                .toCollection(items)
        }
        fileNameProperties["to_be_added_key"] = "to_be_added_value"
        testScope.advanceTimeBy(2000)
        testScope.cancel()
        assertEquals(1, items.size)
    }
}