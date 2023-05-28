package gg.jte.gradle;

import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;

public abstract class JteExtensionSettings {
    public abstract Property<String> getClassName();
    public abstract MapProperty<String, String> getProperties();

    public void property(String key, String value) {
        getProperties().put(key, value);
    }

    public void property(String key, Provider<String> valueProvider) {
        getProperties().put(key, valueProvider);
    }
}
