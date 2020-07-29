package org.jusecase.jte.internal;

import org.jusecase.jte.html.HtmlPolicy;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class RuntimeTemplateLoader extends TemplateLoader {
    private final ClassLoader singleClassLoader;

    public RuntimeTemplateLoader(Path classDirectory) {
        super(classDirectory);
        this.singleClassLoader = createClassLoader();
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
    public void setNullSafeTemplateCode(boolean value) {
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
    public List<String> getTemplatesUsing(String name) {
        return Collections.emptyList();
    }

    @Override
    public void cleanAll() {
        // ignored
    }

    @Override
    public void precompileAll(List<String> compilePath) {
        // ignored
    }

    @Override
    public boolean hasChanged(String name) {
        return false;
    }

}
