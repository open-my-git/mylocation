package akhoi.libs.mlct.tools.java;

import kotlin.jvm.JvmClassMappingKt;
import kotlin.reflect.KClass;

public final class KeyValuePrefsReaderExtensions {
    private KeyValuePrefsReaderExtensions() {
    }

    public static <T> T get(KeyValuePrefsReader reader, String key, KClass<T> kClass) {
        return reader.get(key, kClass);
    }

    public static <T> T get(KeyValuePrefsReader reader, String key, Class<T> clazz) {
        return reader.get(key, JvmClassMappingKt.getKotlinClass(clazz));
    }

    public static <T> boolean compare(KeyValuePrefsReader reader, String key, T value, KClass<T> kClass) {
        T stored = reader.get(key, kClass);
        if (value == null) {
            return stored == null;
        }
        return value.equals(stored);
    }

    public static <T> boolean compare(KeyValuePrefsReader reader, String key, T value, Class<T> clazz) {
        return compare(reader, key, value, JvmClassMappingKt.getKotlinClass(clazz));
    }
}
