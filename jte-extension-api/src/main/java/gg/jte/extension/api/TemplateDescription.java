package gg.jte.extension.api;

import java.util.List;

/**
 * An instance of this class will be given to an extension for each jte template.
 * <p>
 * Implementations must properly implement {@link Object#equals(Object)} and {@link Object#hashCode()}.
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
