package akhoi.libs.mlct.tools

import org.junit.Test
import org.junit.Assert.*

class LRUCacheTest {
    
    @Test
    fun testBasicPutAndGet() {
        val cache = LRUCache<String, Int>(2)
        
        cache["key1"] = 1
        cache["key2"] = 2
        
        assertEquals(1, cache["key1"])
        assertEquals(2, cache["key2"])
        assertEquals(2, cache.size())
    }
    
    @Test
    fun testGetNonExistentKey() {
        val cache = LRUCache<String, Int>(2)
        
        assertNull(cache["nonexistent"])
    }
    
    @Test
    fun testLRUEviction() {
        val cache = LRUCache<String, Int>(2)
        
        cache["key1"] = 1
        cache["key2"] = 2
        cache["key3"] = 3
        
        assertNull(cache["key1"])
        assertEquals(2, cache["key2"])
        assertEquals(3, cache["key3"])
        assertEquals(2, cache.size())
    }
    
    @Test
    fun testUpdateExistingKey() {
        val cache = LRUCache<String, Int>(2)
        
        cache["key1"] = 1
        cache["key2"] = 2
        cache["key1"] = 10
        
        assertEquals(10, cache["key1"])
        assertEquals(2, cache["key2"])
        assertEquals(2, cache.size())
    }
    
    @Test
    fun testGetUpdatesOrder() {
        val cache = LRUCache<String, Int>(2)
        
        cache["key1"] = 1
        cache["key2"] = 2
        
        cache["key1"]
        
        cache["key3"] = 3
        
        assertEquals(1, cache["key1"])
        assertNull(cache["key2"])
        assertEquals(3, cache["key3"])
    }
    
    @Test
    fun testRemove() {
        val cache = LRUCache<String, Int>(2)
        
        cache["key1"] = 1
        cache["key2"] = 2
        
        assertEquals(1, cache.remove("key1"))
        assertNull(cache["key1"])
        assertEquals(2, cache["key2"])
        assertEquals(1, cache.size())
    }
    
    @Test
    fun testRemoveNonExistent() {
        val cache = LRUCache<String, Int>(2)
        
        assertNull(cache.remove("nonexistent"))
    }
    
    @Test
    fun testClear() {
        val cache = LRUCache<String, Int>(2)
        
        cache["key1"] = 1
        cache["key2"] = 2
        
        cache.clear()
        
        assertNull(cache["key1"])
        assertNull(cache["key2"])
        assertEquals(0, cache.size())
    }
    
    @Test
    fun testSingleCapacity() {
        val cache = LRUCache<String, Int>(1)
        
        cache["key1"] = 1
        assertEquals(1, cache["key1"])
        
        cache["key2"] = 2
        assertNull(cache["key1"])
        assertEquals(2, cache["key2"])
    }
}