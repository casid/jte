package gg.jte.extension.api;

/**
 * Instances of this type will be given to an extension as part of TemplateDescriptions.
 */
public interface ParamDescription {
    String type();
    String name();
    String defaultValue();
}
