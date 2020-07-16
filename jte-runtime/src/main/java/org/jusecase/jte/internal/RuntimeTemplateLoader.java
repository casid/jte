package org.jusecase.jte.internal;

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
    protected ClassInfo getClassInfo(String className) {
        return null; // TODO fix
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
