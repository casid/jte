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
    protected final String packageName;

    protected TemplateLoader(Path classDirectory, String packageName) {
        this.classDirectory = classDirectory;
        this.packageName = packageName;
    }

    public Template load(String name) {
        ClassInfo templateInfo = new ClassInfo(name, packageName);

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
            if (stackTraceElement.getClassName().startsWith(packageName)) {
                ClassInfo classInfo = getClassInfo(classLoader, getClassName(stackTraceElement));
                if (classInfo != null) {
                    return new DebugInfo(classInfo.name, resolveLineNumber(classInfo, stackTraceElement.getLineNumber()));
                }
            }
        }

        return null;
    }

    private String getClassName(StackTraceElement stackTraceElement) {
        String className = stackTraceElement.getClassName();
        if (className.endsWith("$Companion")) {
            return className.substring(0, className.length() - "$Companion".length());
        }
        return className;
    }

    protected abstract ClassInfo getClassInfo(ClassLoader classLoader, String className);

    private int resolveLineNumber(ClassInfo classInfo, int lineNumber) {
        if (lineNumber < 0) {
            return 0;
        }

        int[] codeLineToTemplateLine = classInfo.lineInfo;
        int lineIndex = lineNumber - 1;

        if (lineIndex >= codeLineToTemplateLine.length) {
            return 0;
        }

        return codeLineToTemplateLine[lineIndex] + 1;
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
