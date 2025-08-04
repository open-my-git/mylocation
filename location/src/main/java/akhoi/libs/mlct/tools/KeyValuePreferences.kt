package akhoi.libs.mlct.tools

interface KeyValuePreferences : KeyValuePrefsReader {
    operator fun <T : Any> set(key: String, value: T?)
    fun remove(key: String)
}
