package akhoi.libs.tools

import org.junit.After
import org.junit.Before
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MetadataPropertiesTest {
    lateinit var metadataProperties: MetadataProperties
    lateinit var tempDir: File

    @Before
    fun setup() {
        val tempDirPath = createTempDirectory()
        tempDir = File(tempDirPath.toString())
        metadataProperties = MetadataProperties(tempDir, "test_cache_properties")
        metadataProperties.initialize()
    }

    @After
    fun tearDown() {
        metadataProperties.clear()
        tempDir.deleteRecursively()
    }

    @Test
    fun testLong_normal() {
        metadataProperties.put("long_key", 12912840L)
        assertEquals(12912840L, metadataProperties.get("long_key"))
    }

    @Test
    fun testLong_zero() {
        metadataProperties.put("long_key", 0L)
        assertEquals(0L, metadataProperties.get("long_key"))
    }

    @Test
    fun testLong_random() {
        val randomLong = Random(System.currentTimeMillis()).nextLong()
        metadataProperties.put("random_long_key", randomLong)
        assertEquals(randomLong, metadataProperties.get("random_long_key"))
    }

    @Test
    fun testLong_remove() {
        metadataProperties.put("long_key", 100L)
        assertEquals(100L, metadataProperties.get("long_key"))
        metadataProperties.remove("long_key")
        assertEquals(null as Long?, metadataProperties.get("long_key"))
    }

    @Test
    fun testString_empty() {
        metadataProperties.put("string_key", "")
        assertEquals("", metadataProperties.get("string_key"))
    }

    @Test
    fun testString_random() {
        val chars =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+-=[]{}|;:,.<>?"
        val randomString = (1..100).map { chars.random() }.joinToString("")
        metadataProperties.put("random_string_key", randomString)
        assertEquals(randomString, metadataProperties.get("random_string_key"))
    }

    @Test
    fun testString_remove() {
        metadataProperties.put("string_key", "string_value")
        assertEquals("string_value", metadataProperties.get("string_key"))
        metadataProperties.remove("string_key")
        assertEquals(null, metadataProperties.get<String>("string_key"))
    }

    @Test
    fun testString_valueOverwritten() {
        metadataProperties.put("string_key", "string_value_first")
        assertEquals("string_value_first", metadataProperties.get("string_key"))
        metadataProperties.put("string_key", "string_value_second")
        assertEquals("string_value_second", metadataProperties.get<String>("string_key"))
    }

    @Test
    fun testGet_withoutPut() {
        assertEquals(null, metadataProperties.get<Long>("key"))
    }

    @Test
    fun testRemove_withoutPut() {
        assertFalse(metadataProperties.contains("nonexistent_key"))
        metadataProperties.remove("nonexistent_key")
        assertFalse(metadataProperties.contains("nonexistent_key"))
    }

    @Test
    fun testContains() {
        assertFalse(metadataProperties.contains("to_be_added_key"))
        metadataProperties.put("to_be_added_key", "to_be_added_value")
        assertTrue(metadataProperties.contains("to_be_added_key"))
    }
}