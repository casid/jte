package gg.jte.compiler;

public final class TemplateDependencyShared {
    private final String name;
    private long lastCompiledTimestamp;

    public TemplateDependencyShared(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setLastCompiledTimestamp(long lastCompiledTimestamp) {
        this.lastCompiledTimestamp = lastCompiledTimestamp;
    }

    public long getLastCompiledTimestamp() {
        return lastCompiledTimestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TemplateDependencyShared that = (TemplateDependencyShared) o;

        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
