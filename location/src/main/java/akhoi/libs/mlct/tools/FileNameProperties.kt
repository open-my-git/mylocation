package akhoi.libs.mlct.tools

import java.io.File
import kotlin.reflect.KClass

class FileNameProperties(locationDir: File, name: String) {
    private val propsDir: File = File("$locationDir/$name")

    init {
        propsDir.mkdirs()
    }

    inline operator fun <reified T : Any> get(key: String): T? = get(key, T::class)
    inline operator fun <reified T : Any> set(key: String, value: T?) = put(key, value, T::class)
    inline fun <reified T : Any> put(key: String, value: T) = put(key, value, T::class)

    @Synchronized
    fun contains(key: String): Boolean = propsDir.resolve(key).exists()

    @Synchronized
    fun remove(key: String) {
        propsDir.resolve(key).delete()
    }

    @Synchronized
    fun <T : Any> get(key: String, klazz: KClass<T>): T? {
        val keyDir = propsDir.resolve(key)
        if (!keyDir.exists()) {
            return null
        }
        val valueString = keyDir.listFiles()?.firstOrNull()?.name

        @Suppress("UNCHECKED_CAST")
        return when (klazz) {
            String -> valueString
            Byte -> valueString?.toByteOrNull()
            UByte -> valueString?.toUByteOrNull()
            Short -> valueString?.toShortOrNull()
            UShort -> valueString?.toUShortOrNull()
            Int -> valueString?.toIntOrNull()
            UInt -> valueString?.toUIntOrNull()
            Long -> valueString?.toLongOrNull()
            ULong -> valueString?.toULongOrNull()
            Float -> valueString?.toFloatOrNull()
            Double -> valueString?.toDoubleOrNull()
            else -> null
        } as T?
    }

    @Synchronized
    fun <T : Any> put(key: String, value: T?, klazz: KClass<T>? = null) {
        if (value == null) {
            remove(key)
            return
        }

        val valueString = value.toString()
        if (valueString.length > 255) {
            return
        }

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

    @Synchronized
    fun clear() {
        propsDir.listFiles()?.forEach { it.deleteRecursively() }
    }
}
