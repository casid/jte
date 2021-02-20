package gg.jte.runtime;

import gg.jte.TemplateException;
import gg.jte.TemplateNotFoundException;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.List;

public abstract class TemplateLoader {
    protected final Path classDirectory;

    protected TemplateLoader(Path classDirectory) {
        this.classDirectory = classDirectory;
    }

    public Template load(String name) {
        ClassInfo templateInfo = new ClassInfo(name, Constants.PACKAGE_NAME);

        TemplateType templateType = getTemplateType(name);

        try {
            Class<?> clazz = getClassLoader().loadClass(templateInfo.fullName);
            return new Template(name, templateType, clazz);
        } catch (Exception e) {
            throw new TemplateNotFoundException("Failed to load " + name, e);
        }
    }

    public DebugInfo resolveDebugInfo(ClassLoader classLoader, StackTraceElement[] stackTrace) {
        if (stackTrace.length == 0) {
            return null;
        }

        for (StackTraceElement stackTraceElement : stackTrace) {
            if (stackTraceElement.getClassName().startsWith(Constants.PACKAGE_NAME)) {
                ClassInfo classInfo = getClassInfo(classLoader, stackTraceElement.getClassName());
                if (classInfo != null) {
                    return new DebugInfo(classInfo.name, resolveLineNumber(classInfo, stackTraceElement.getLineNumber()));
                }
            }
        }

        return null;
    }

    protected abstract ClassInfo getClassInfo(ClassLoader classLoader, String className);

    private int resolveLineNumber(ClassInfo classInfo, int lineNumber) {
        int[] javaLineToTemplateLine = classInfo.lineInfo;
        int lineIndex = lineNumber - 1;

        if (lineIndex >= javaLineToTemplateLine.length) {
            return 0;
        }

        return javaLineToTemplateLine[lineIndex] + 1;
    }

    protected TemplateType getTemplateType(String name) {
        if (name.startsWith(Constants.TAG_DIRECTORY)) {
            return TemplateType.Tag;
        }

        if (name.startsWith(Constants.LAYOUT_DIRECTORY)) {
            return TemplateType.Layout;
        }

        return TemplateType.Template;
    }

    protected abstract ClassLoader getClassLoader();

    protected ClassLoader createClassLoader(ClassLoader parentClassLoader) {
        try {
            URL[] urls = {classDirectory.toUri().toURL()};
            if (parentClassLoader == null) {
                return new URLClassLoader(urls);
            } else {
                return new URLClassLoader(urls, parentClassLoader);
            }
        } catch (MalformedURLException e) {
            throw new TemplateException("Failed to create class loader for " + classDirectory, e);
        }
    }

    public abstract List<String> getTemplatesUsing(String name);

    public abstract void cleanAll();

    public abstract List<String> generateAll();

    public abstract List<String> precompileAll(List<String> compilePath);

    public abstract boolean hasChanged(String name);
}
