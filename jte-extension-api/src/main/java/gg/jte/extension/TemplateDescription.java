package gg.jte.extension;

import java.util.List;

/**
 * An instance of this class will be given to an extension for each jte template.
 */
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
