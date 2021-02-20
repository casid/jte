package gg.jte.compiler;

import gg.jte.runtime.ClassInfo;

import java.util.List;

public final class ClassDefinition {
    private final String name;
    private final String extension;
    private String code;
    private List<byte[]> binaryTextParts;

    public ClassDefinition(String name, ClassInfo classInfo) {
        this.name = name;
        this.extension = "kte".equals(classInfo.extension) ? "kt" : "java";
    }

    public ClassDefinition(String name, String extension) {
        this.name = name;
        this.extension = extension;
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

    public String getSourceFileName() {
        return getName().replace('.', '/') + "." + extension;
    }

    public String getBinaryTextPartsFileName() {
        return getName().replace('.', '/') + ".bin";
    }

    public String getExtension() {
        return extension;
    }
}
