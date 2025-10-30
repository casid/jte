package gg.jte.gradle;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

final class Utils {
    private Utils() {} // Utility class

    public static URLClassLoader createCompilerClassLoader(ConfigurableFileCollection compilePath) {
        try {
            List<URL> urls = new ArrayList<>();
            for (File file : compilePath.getFiles()) {
                urls.add(file.toURI().toURL());
            }
            return new URLClassLoader(urls.toArray(new URL[0]), Thread.currentThread().getContextClassLoader());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create compiler classloader", e);
        }
    }

    static String[] toStringArrayOrNull(ListProperty<String> value) {
        return value.map(l -> l.toArray(String[]::new)).getOrNull();
    }

    static Path toPathOrNull(DirectoryProperty directory) {
        return directory.map(d -> d.getAsFile().toPath()).getOrNull();
    }
}
