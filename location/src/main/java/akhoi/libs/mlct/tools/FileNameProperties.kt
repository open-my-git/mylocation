package akhoi.libs.mlct.tools

import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import java.io.IOException
import kotlin.io.path.name
import kotlin.reflect.KClass

class FileNameProperties(
    private val propsDir: File,
    private val keyWatcher: FileWatcher = FileWatcher(propsDir)
) : KeyValuePreferences {
    private val valueConverters = mutableMapOf<KClass<*>, ValueConverter<*>>()

    init {
        propsDir.mkdirs()
        registerValueConverters()
    }

    private fun registerValueConverters() {
        valueConverters[Byte::class] = ValueConverter { it.toByteOrNull() }
        valueConverters[UByte::class] = ValueConverter { it.toUByteOrNull() }
        valueConverters[Short::class] = ValueConverter { it.toShortOrNull() }
        valueConverters[UShort::class] = ValueConverter { it.toUShortOrNull() }
        valueConverters[Int::class] = ValueConverter { it.toIntOrNull() }
        valueConverters[UInt::class] = ValueConverter { it.toUIntOrNull() }
        valueConverters[Long::class] = ValueConverter { it.toLongOrNull() }
        valueConverters[ULong::class] = ValueConverter { it.toULongOrNull() }
        valueConverters[Float::class] = ValueConverter { it.toFloatOrNull() }
        valueConverters[Double::class] = ValueConverter { it.toDoubleOrNull() }
        valueConverters[String::class] = ValueConverter { it }
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

        val keyFile = keyDir.listFiles()?.firstOrNull()
        val keyFileName = keyFile?.name ?: ""
        val stringValue = if (keyFile != null && keyFile.length() > 0L) {
            keyFile.bufferedReader().use {
                it.readText()
            }
        } else {
            keyFileName
        }
        @Suppress("UNCHECKED_CAST")
        return valueConverters[klazz]?.invoke(stringValue) as T?
    }

    override fun <T : Any> flowGet(
        key: String,
        klazz: KClass<T>
    ): Flow<T?> = keyWatcher.watchFile(key)
        .map { (type, path) ->
            @Suppress("UNCHECKED_CAST")
            when (type) {
                FileWatcher.EventType.CREATED, FileWatcher.EventType.UPDATED ->
                    valueConverters[klazz]?.invoke(path.name) as T?
                FileWatcher.EventType.DELETED -> null
            }
        }

    @Synchronized
    override operator fun <T : Any> set(key: String, value: T?) {
        val keyDir = propsDir.resolve(key)
        if (value == null) {
            keyDir.deleteRecursively()
            return
        }

        keyDir.mkdir()
        val stringValue = value.toString()
        if (stringValue.isEmpty()) {
            val keyFile = keyDir.listFiles()?.firstOrNull()
            keyFile?.delete()
            return
        }

        writeFile(key, stringValue)
    }

    private fun writeFile(key: String, value: String) {
        val keyDir = propsDir.resolve(key)
        val keyFile = keyDir.listFiles()?.firstOrNull()
        val isKeyFileExisted = keyFile?.exists() == true
        if (isKeyFileExisted && keyFile.length() == 0L && keyFile.name == value) {
            return
        }

        val writeStringToFile: (f: File, s: String) -> Unit = { f, s ->
            f.bufferedWriter().use { it.write(s) }
        }

        val keyContentFile = File("$keyDir/$key")
        if (value.length > MAX_VALUE_LEN) {
            keyFile?.delete()
            writeStringToFile(keyContentFile, value)
            return
        }

        keyContentFile.delete()
        val newKeyFile = File("$keyDir/$value")
        if (isKeyFileExisted) {
            if (!keyFile.renameTo(newKeyFile)) {
                keyFile.delete()
                writeStringToFile(keyContentFile, value)
            }
        } else {
            try {
                newKeyFile.createNewFile()
            } catch (_: IOException) {
                writeStringToFile(keyContentFile, value)
            }
        }
    }

    @Synchronized
    fun clear() {
        propsDir.listFiles()?.forEach { it.deleteRecursively() }
    }

    private fun interface ValueConverter<T> {
        fun invoke(stringValue: String): T?
    }

    companion object {
        @VisibleForTesting
        const val MAX_VALUE_LEN = 255
    }
}
