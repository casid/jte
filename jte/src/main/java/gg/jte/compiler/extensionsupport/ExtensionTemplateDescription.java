package gg.jte.compiler.extensionsupport;

import gg.jte.compiler.ClassDefinition;
import gg.jte.extension.ParamDescription;
import gg.jte.extension.TemplateDescription;
import gg.jte.runtime.ClassInfo;

import java.util.ArrayList;
import java.util.List;

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
        return new ArrayList<>(classDefinition.getParams());
    }

    @Override
    public List<String> imports() {
        return classDefinition.getImports();
    }


}
