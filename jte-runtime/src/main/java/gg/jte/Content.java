package gg.jte;

public interface Content {
    void writeTo(TemplateOutput output);

    default boolean isEmpty() {
        return false;
    }
}
