package akhoi.libs.tools

import akhoi.libs.mlct.tools.FileNameProperties
import akhoi.libs.mlct.tools.get
import org.junit.After
import org.junit.Before
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

    @Before
    fun setup() {
        val tempDirPath = createTempDirectory()
        tempDir = File(tempDirPath.toString())
        fileNameProperties = FileNameProperties(tempDir, "test_cache_properties")
    }

    @After
    fun tearDown() {
        fileNameProperties.clear()
        tempDir.deleteRecursively()
    }

    @Test
    fun testLong_normal() {
        fileNameProperties.put("long_key", 12912840L)
        assertEquals(12912840L, fileNameProperties.get("long_key"))
    }

    @Test
    fun testLong_zero() {
        fileNameProperties.put("long_key", 0L)
        assertEquals(0L, fileNameProperties.get("long_key"))
    }

    @Test
    fun testLong_random() {
        val randomLong = Random(System.currentTimeMillis()).nextLong()
        fileNameProperties.put("random_long_key", randomLong)
        assertEquals(randomLong, fileNameProperties.get("random_long_key"))
    }

    @Test
    fun testLong_remove() {
        fileNameProperties.put("long_key", 100L)
        assertEquals(100L, fileNameProperties.get("long_key"))
        fileNameProperties.remove("long_key")
        assertEquals(null as Long?, fileNameProperties.get("long_key"))
    }

    @Test
    fun testString_empty() {
        fileNameProperties.put("string_key", "")
        assertEquals("", fileNameProperties.get("string_key"))
    }

    @Test
    fun testString_random() {
        val chars =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+-=[]{}|;:,.<>?"
        val randomString = (1..100).map { chars.random() }.joinToString("")
        fileNameProperties.put("random_string_key", randomString)
        assertEquals(randomString, fileNameProperties.get("random_string_key"))
    }

    @Test
    fun testString_remove() {
        fileNameProperties.put("string_key", "string_value")
        assertEquals("string_value", fileNameProperties.get("string_key"))
        fileNameProperties.remove("string_key")
        assertEquals(null, fileNameProperties.get<String>("string_key"))
    }

    @Test
    fun testString_valueOverwritten() {
        fileNameProperties.put("string_key", "string_value_first")
        assertEquals("string_value_first", fileNameProperties.get("string_key"))
        fileNameProperties.put("string_key", "string_value_second")
        assertEquals("string_value_second", fileNameProperties.get<String>("string_key"))
    }

    @Test
    fun testGet_withoutPut() {
        assertEquals(null, fileNameProperties.get<Long>("key"))
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
        fileNameProperties.put("to_be_added_key", "to_be_added_value")
        assertTrue(fileNameProperties.contains("to_be_added_key"))
    }
}