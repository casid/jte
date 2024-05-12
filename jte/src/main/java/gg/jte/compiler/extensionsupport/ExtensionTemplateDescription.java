package gg.jte.compiler.extensionsupport;

import gg.jte.compiler.ClassDefinition;
import gg.jte.extension.api.ParamDescription;
import gg.jte.extension.api.TemplateDescription;
import gg.jte.runtime.ClassInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ExtensionTemplateDescription implements TemplateDescription {
    private final ClassDefinition classDefinition;
    private final ClassInfo classInfo;

    public ExtensionTemplateDescription(ClassDefinition classDefinition, ClassInfo classInfo) {

        this.classDefinition = classDefinition;
        this.classInfo = classInfo;
    }
    @Override
    public String name() {
        return classInfo.name;
    }

    @Override
    public String packageName() {
        return classInfo.packageName;
    }

    @Override
    public String className() {
        return classInfo.className;
    }

    @Override
    public List<ParamDescription> params() {
        return List.copyOf(classDefinition.getParams());
    }

    @Override
    public List<String> imports() {
        return classDefinition.getImports();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExtensionTemplateDescription that = (ExtensionTemplateDescription) o;

        if (!classDefinition.equals(that.classDefinition)) return false;
        return classInfo.equals(that.classInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classDefinition, classInfo);
    }
}
