package gg.jte.compiler;

import gg.jte.extension.api.ParamDescription;

public final class ParamInfo implements ParamDescription {
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

    @Override
    public String type() {
        return type;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String defaultValue() {
        return defaultValue;
    }
}
