package gg.jte.compiler;

public final class ParamInfo {
    public final String type;
    public final String name;
    public final String defaultValue;
    public final boolean varargs;
    public final int templateLine;

    public ParamInfo(String type, String name, String defaultValue, boolean varargs, int templateLine) {
        this.type = type;
        this.name = name;
        this.defaultValue = defaultValue;
        this.varargs = varargs;
        this.templateLine = templateLine;
    }
}
