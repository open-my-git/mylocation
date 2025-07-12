package akhoi.libs.tools

import androidx.annotation.VisibleForTesting
import java.io.File
import kotlin.reflect.KClass

class MetadataProperties(locationDir: File, name: String) {
    private val propsDir: File = File("$locationDir/$name")
    private val cache: LRUCache<String, Any> = LRUCache(100)

    @Suppress("UNCHECKED_CAST")
    private fun <T> readCache(key: String): T? = cache[key] as? T

    fun initialize() {
        propsDir.mkdirs()
    }

    inline fun <reified T : Any> get(key: String): T? = get(key, T::class)
    inline fun <reified T : Any> put(key: String, value: T) = put(key, value, T::class)

    @Synchronized
    fun contains(key: String): Boolean =
        cache[key] != null || propsDir.resolve(key).exists()

    @Synchronized
    fun remove(key: String) {
        cache.remove(key)
        propsDir.resolve(key).delete()
    }

    @Synchronized
    fun <T : Any> get(key: String, klazz: KClass<T>): T? {
        val cacheHit: T? = readCache(key)
        if (cacheHit != null) {
            return cacheHit
        }

        val value: T? = readFile(key, klazz)
        if (value != null) {
            cache[key] = value
        }

        return value
    }

    @Synchronized
    fun <T : Any> put(key: String, value: T, klazz: KClass<T>? = null) {
        if (cache[key] == value) {
            return
        }
        val valueString = value.toString()
        if (valueString.length > 255) {
            return
        }

        cache[key] = value
        writeFile(key, valueString)
    }

    private fun writeFile(key: String, valueString: String) {
        val keyDir = propsDir.resolve(key)
        if (keyDir.exists()) {
            keyDir.listFiles()?.forEach { it.delete() }
        } else {
            keyDir.mkdir()
        }
        File("$keyDir/$valueString").createNewFile()
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> readFile(key: String, klazz: KClass<T>): T? {
        val keyDir = propsDir.resolve(key)
        if (!keyDir.exists()) {
            return null
        }
        val valueString = keyDir.listFiles()?.firstOrNull()?.name
        return when (klazz) {
            String -> valueString
            Long -> valueString?.toLongOrNull()
            Byte -> valueString?.toByteOrNull()
            else -> null
        } as T?
    }

    @Synchronized
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun clear() {
        cache.clear()
        propsDir.deleteRecursively()
    }
}
