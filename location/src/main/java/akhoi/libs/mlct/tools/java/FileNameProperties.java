package akhoi.libs.mlct.tools.java;

import java.io.File;

import kotlinx.coroutines.flow.Flow;
import kotlin.reflect.KClass;

public class FileNameProperties implements KeyValuePreferences {
    private final akhoi.libs.mlct.tools.FileNameProperties delegate;

    public FileNameProperties(File propsDir) {
        this.delegate = new akhoi.libs.mlct.tools.FileNameProperties(propsDir);
    }

    public FileNameProperties(File propsDir, FileWatcher keyWatcher) {
        this.delegate = new akhoi.libs.mlct.tools.FileNameProperties(propsDir, keyWatcher.getDelegate());
    }

    @Override
    public synchronized boolean contains(String key) {
        return delegate.contains(key);
    }

    @Override
    public synchronized void remove(String key) {
        delegate.remove(key);
    }

    @Override
    public synchronized <T> T get(String key, KClass<T> klazz) {
        return delegate.get(key, klazz);
    }

    @Override
    public <T> Flow<T> flowGet(String key, KClass<T> klazz) {
        return delegate.flowGet(key, klazz);
    }

    @Override
    public synchronized void set(String key, Object value) {
        delegate.set(key, value);
    }

    public synchronized void clear() {
        delegate.clear();
    }
}
