package gg.jte.internal;

import gg.jte.html.HtmlPolicy;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class RuntimeTemplateLoader extends TemplateLoader {
    private final ClassLoader singleClassLoader;

    public RuntimeTemplateLoader(Path classDirectory, ClassLoader parentClassLoader) {
        super(classDirectory);
        this.singleClassLoader = createClassLoader(parentClassLoader);
    }

    @Override
    protected ClassLoader createClassLoader(ClassLoader parentClassLoader) {
        if (classDirectory == null) {
            return Thread.currentThread().getContextClassLoader();
        }
        return super.createClassLoader(parentClassLoader);
    }

    @Override
    protected ClassInfo getClassInfo(ClassLoader classLoader, String className) {
        try {
            Class<?> clazz = classLoader.loadClass(className);

            ClassInfo classInfo = new ClassInfo((String)clazz.getField(Constants.NAME_FIELD).get(null), "");
            classInfo.lineInfo = (int[]) clazz.getField(Constants.LINE_INFO_FIELD).get(null);
            return classInfo;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected ClassLoader getClassLoader() {
        return singleClassLoader;
    }

    @Override
    public void setTrimControlStructures(boolean value) {
        // ignored
    }

    @Override
    public void setHtmlPolicy(HtmlPolicy htmlPolicy) {
        // ignored
    }

    @Override
    public void setHtmlTags(String[] htmlTags) {
        // ignored
    }

    @Override
    public void setHtmlAttributes(String[] htmlAttributes) {
        // ignored
    }

    @Override
    public void setCompileArgs(String[] compileArgs) {
        // Ignored
    }

    @Override
    public List<String> getTemplatesUsing(String name) {
        return Collections.emptyList();
    }

    @Override
    public void cleanAll() {
        // ignored
    }

    @Override
    public int generateAll() {
        // ignored
        return 0;
    }

    @Override
    public int precompileAll(List<String> compilePath) {
        // ignored
        return 0;
    }

    @Override
    public boolean hasChanged(String name) {
        return false;
    }

}
