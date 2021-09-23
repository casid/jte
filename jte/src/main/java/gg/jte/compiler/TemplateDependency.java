package gg.jte.compiler;

public final class TemplateDependency {
    private final String name;
    private final long lastCompiledTimestamp;

    public TemplateDependency(String name, long lastCompiledTimestamp) {
        this.name = name;
        this.lastCompiledTimestamp = lastCompiledTimestamp;
    }

    public String getName() {
        return name;
    }

    public long getLastCompiledTimestamp() {
        return lastCompiledTimestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TemplateDependency that = (TemplateDependency) o;

        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
