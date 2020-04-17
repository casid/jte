package org.jusecase.jte.internal;

final class ClassDefinition {
    private final String name;
    private String code;

    ClassDefinition(String name) {
        this.name = name;
    }

    void setCode(String code) {
        this.code = code;
    }

    String getCode() {
        return code;
    }

    String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClassDefinition that = (ClassDefinition) o;

        if (!name.equals(that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
