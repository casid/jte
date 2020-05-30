package org.jusecase.jte.internal;

public final class ClassDefinition {
    private final String name;
    private String code;

    ClassDefinition(String name) {
        this.name = name;
    }

    void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClassDefinition that = (ClassDefinition) o;

        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public String getJavaFileName() {
        return getName().replace('.', '/') + ".java";
    }

    public String getClassFileName() {
        return getName().replace('.', '/') + ".class";
    }
}
