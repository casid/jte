package gg.jte.compiler;

import gg.jte.runtime.ClassInfo;

import java.util.List;

public final class ClassDefinition {
    private final String name;
    private final String extension;
    private String code;
    private List<byte[]> binaryTextParts;
    private boolean changed = true;
    private List<ParamInfo> params;
    private List<String> imports;

    public ClassDefinition(String name, ClassInfo classInfo) {
        this.name = name;
        this.extension = "kte".equals(classInfo.extension) ? "kt" : "java";
    }

    public ClassDefinition(String name, String extension) {
        this.name = name;
        this.extension = extension;
    }

    void setCode(String code, List<byte[]> binaryTextParts, List<ParamInfo> params, List<String> imports) {
        this.code = code;
        this.binaryTextParts = binaryTextParts;
        this.params = params;
        this.imports = imports;
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

        if (!name.equals(that.name)) return false;
        return extension.equals(that.extension);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + extension.hashCode();
        return result;
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

    public boolean isChanged() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    public List<ParamInfo> getParams() {
        return params;
    }

    public List<String> getImports() {
        return imports;
    }
}
