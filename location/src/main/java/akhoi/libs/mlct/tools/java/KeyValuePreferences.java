package akhoi.libs.mlct.tools.java;

public interface KeyValuePreferences extends akhoi.libs.mlct.tools.KeyValuePreferences, KeyValuePrefsReader {
    @Override
    void set(String key, Object value);

    @Override
    void remove(String key);
}
