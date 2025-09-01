package akhoi.libs.mlct.tools

import kotlinx.coroutines.flow.Flow
import kotlin.reflect.KClass

interface KeyValuePrefsReader {
    fun <T : Any> get(key: String, klazz: KClass<T>): T?
    fun <T : Any> flowGet(key: String, klazz: KClass<T>): Flow<T?>
    fun contains(key: String): Boolean
}

inline operator fun <reified T : Any> KeyValuePrefsReader.get(key: String): T? =
    get(key, T::class)

inline fun <reified T : Any> KeyValuePrefsReader.compare(key: String, value: T): Boolean =
    get(key, T::class) == value
