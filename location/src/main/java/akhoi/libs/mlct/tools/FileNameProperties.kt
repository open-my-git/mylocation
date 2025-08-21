package akhoi.libs.mlct.tools

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import kotlin.io.path.name
import kotlin.reflect.KClass

class FileNameProperties(
    private val propsDir: File,
) : KeyValuePreferences {
    private val valueConverters = mutableMapOf<KClass<*>, ValueConverter<*>>()
    private val keyWatcher: FileWatcher = FileWatcher(propsDir)

    init {
        propsDir.mkdirs()
        registerValueConverters()
    }

    private fun registerValueConverters() {
        valueConverters[Byte::class] = ValueConverter { it?.toByteOrNull() }
        valueConverters[UByte::class] = ValueConverter { it?.toUByteOrNull() }
        valueConverters[Short::class] = ValueConverter { it?.toShortOrNull() }
        valueConverters[UShort::class] = ValueConverter { it?.toUShortOrNull() }
        valueConverters[Int::class] = ValueConverter { it?.toIntOrNull() }
        valueConverters[UInt::class] = ValueConverter { it?.toUIntOrNull() }
        valueConverters[Long::class] = ValueConverter { it?.toLongOrNull() }
        valueConverters[ULong::class] = ValueConverter { it?.toULongOrNull() }
        valueConverters[Float::class] = ValueConverter { it?.toFloatOrNull() }
        valueConverters[Double::class] = ValueConverter { it?.toDoubleOrNull() }
    }

    @Synchronized
    override fun contains(key: String): Boolean = propsDir.resolve(key).exists()

    @Synchronized
    override fun remove(key: String) {
        keyWatcher.unwatchFile(key)
        propsDir.resolve(key).deleteRecursively()
    }

    @Synchronized
    override fun <T : Any> get(key: String, klazz: KClass<T>): T? {
        val keyDir = propsDir.resolve(key)
        if (!keyDir.exists()) {
            return null
        }
        val stringValue = keyDir.listFiles()?.firstOrNull()?.name

        return convertValue(stringValue, klazz)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> convertValue(stringValue: String?, klazz: KClass<T>): T? =
        valueConverters[klazz]?.invoke(stringValue) as T?

    override fun <T : Any> flowGet(
        key: String,
        klazz: KClass<T>
    ): Flow<T?> = keyWatcher.watchFile(key)
        .map { (_, path) -> valueConverters[klazz]?.invoke(path.name) as T? }

    @Synchronized
    override operator fun <T : Any> set(key: String, value: T?) {
        val keyDir = propsDir.resolve(key)
        val currentKey = keyDir.listFiles()?.firstOrNull()
        if (value == null) {
            if (currentKey?.exists() == true) {
                currentKey.delete()
            }
            return
        }

        val stringValue = value.toString()
        if (stringValue.length > 255) {
            return
        }

        writeFile(key, stringValue)
    }

    private fun writeFile(key: String, stringValue: String) {
        val keyDir = propsDir.resolve(key)
        if (!keyDir.exists()) {
            keyDir.mkdir()
        }
        val currentKey = keyDir.listFiles()?.firstOrNull()
        val newKey = File("$keyDir/$stringValue")
        if (currentKey?.exists() == true && currentKey.name != stringValue) {
            if (!currentKey.renameTo(newKey)) {
                currentKey.delete()
            }
        }
        if (!newKey.exists()) {
            newKey.createNewFile()
        }
    }

    @Synchronized
    fun clear() {
        propsDir.listFiles()?.forEach { it.deleteRecursively() }
    }

    private fun interface ValueConverter<T> {
        fun invoke(stringValue: String?): T?
    }
}
