package gg.jte.extension;

import java.nio.file.Path;
import java.util.List;

public interface TemplateDescription {
    String name();
    String packageName();
    String className();

    default String fullyQualifiedClassName() {
        return packageName() + "." + className();
    }
    List<ParamDescription> params();
    List<String> imports();
}
