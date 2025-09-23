package akhoi.libs.mlct.tools.java;

import kotlinx.coroutines.flow.Flow;
import kotlin.reflect.KClass;

public interface KeyValuePrefsReader extends akhoi.libs.mlct.tools.KeyValuePrefsReader {
    @Override
    <T> T get(String key, KClass<T> klazz);

    @Override
    <T> Flow<T> flowGet(String key, KClass<T> klazz);

    @Override
    boolean contains(String key);
}
