package gg.jte.compiler;

public final class ParamInfo {
    public final String type;
    public final String name;
    public final String defaultValue;
    public final boolean varargs;

    public ParamInfo(String type, String name, String defaultValue, boolean varargs) {
        this.type = type;
        this.name = name;
        this.defaultValue = defaultValue;
        this.varargs = varargs;
    }
}
