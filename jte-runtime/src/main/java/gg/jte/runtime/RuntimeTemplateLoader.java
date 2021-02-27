package gg.jte.runtime;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class RuntimeTemplateLoader extends TemplateLoader {
    private final ClassLoader singleClassLoader;

    public RuntimeTemplateLoader(Path classDirectory, ClassLoader parentClassLoader, String packageName) {
        super(classDirectory, packageName);
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
    public List<String> getTemplatesUsing(String name) {
        return Collections.emptyList();
    }

    @Override
    public void cleanAll() {
        // ignored
    }

    @Override
    public List<String> generateAll() {
        // ignored
        return null;
    }

    @Override
    public List<String> precompileAll(List<String> compilePath) {
        // ignored
        return null;
    }

    @Override
    public boolean hasChanged(String name) {
        return false;
    }

}
