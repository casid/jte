package gg.jte.gradle;

import org.gradle.api.file.ConfigurableFileCollection;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public final class ClassLoaderUtils {
    private ClassLoaderUtils() {} // Utility class

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
}
