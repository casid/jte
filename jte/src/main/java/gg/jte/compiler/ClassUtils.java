package gg.jte.compiler;

import gg.jte.TemplateException;
import gg.jte.runtime.StringUtils;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.function.Consumer;

public final class ClassUtils {

    private ClassUtils() {
    }

    public static String join(List<String> classPath) {
        return String.join(File.pathSeparator, classPath);
    }

    public static void resolveClasspathFromClassLoader(ClassLoader classLoader, Consumer<String> pathConsumer) {
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }

        String separator = File.pathSeparator;
        String classPath = System.getProperty("java.class.path");

        if (!StringUtils.isBlank(classPath)) {
            String[] paths = classPath.split(separator);
            for (String path : paths) {
                pathConsumer.accept(path);
            }
        }
        String modulePath = System.getProperty("jdk.module.path");

        if (!StringUtils.isBlank(modulePath)) {
            String[] paths = modulePath.split(separator);
            for (String path : paths) {
                pathConsumer.accept(path);
            }
        }

        if (classLoader instanceof URLClassLoader loader) {
            for (URL url : loader.getURLs()) {
                String protocol = url.getProtocol();

                if ("file".equalsIgnoreCase(protocol)) {
                    try {
                        pathConsumer.accept(new File(url.toURI()).toString());
                    } catch (URISyntaxException e) {
                        throw new TemplateException("Failed to append classpath for " + url, e);
                    }
                } else if ("jar".equalsIgnoreCase(protocol)) {
                    throw new TemplateException("For self contained applications jte templates must be precompiled. See https://jte.gg/pre-compiling/#using-the-application-class-loader for more information.");
                }
            }
        }
    }
}
