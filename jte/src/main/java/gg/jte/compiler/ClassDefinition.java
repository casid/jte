package gg.jte.compiler;

import java.util.List;

public final class ClassDefinition {
    private final String name;
    private String code;
    private List<byte[]> binaryTextParts;

    public ClassDefinition(String name) {
        this.name = name;
    }

    void setCode(String code, List<byte[]> binaryTextParts) {
        this.code = code;
        this.binaryTextParts = binaryTextParts;
    }

    public String getCode() {
        return code;
    }

    public List<byte[]> getBinaryTextParts() {
        return binaryTextParts;
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

    public String getBinaryTextPartsFileName() {
        return getName().replace('.', '/') + ".bin";
    }
}
