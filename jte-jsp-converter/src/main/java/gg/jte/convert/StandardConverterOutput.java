package gg.jte.convert;

public class StandardConverterOutput implements ConverterOutput {

    private final StringBuilder buffer = new StringBuilder();

    private boolean trim = false;
    private boolean insideScript = false;

    @Override
    public ConverterOutput append(String s) {
        if (s != null) {
            buffer.append(s);
        }
        return this;
    }

    @Override
    public ConverterOutput prepend(String s) {
        if (s != null) {
            buffer.insert(0, s);
        }
        return this;
    }

    @Override
    public boolean isTrimWhitespace() {
        return trim;
    }

    @Override
    public void setTrimWhitespace(boolean value) {
        trim = value;
    }

    @Override
    public boolean isInsideScript() {
        return insideScript;
    }

    @Override
    public void setInsideScript(boolean value) {
        insideScript = value;
    }

    @Override
    public String toString() {
        return buffer.toString();
    }
}
