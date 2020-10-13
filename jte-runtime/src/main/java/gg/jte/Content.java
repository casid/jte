package gg.jte;

public interface Content {
    void writeTo(TemplateOutput output);

    default boolean isEmptyContent() {
        return false;
    }
}
