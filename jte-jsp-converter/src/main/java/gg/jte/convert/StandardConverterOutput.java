package gg.jte.convert;

public class StandardConverterOutput implements ConverterOutput {
    private final StringBuilder buffer = new StringBuilder();

    private boolean trim = false;
    private boolean insideScript = false;

    @Override
    public ConverterOutput append(String s) {
        buffer.append(s);

        return this;
    }

    @Override
    public void setTrimWhitespace(boolean value) {
        trim = value;
    }

    @Override
    public boolean isTrimWhitespace() {
        return trim;
    }

    @Override
    public void setInsideScript(boolean value) {
        insideScript = value;
    }

    @Override
    public boolean isInsideScript() {
        return insideScript;
    }

    @Override
    public String toString() {
        return buffer.toString();
    }
}
