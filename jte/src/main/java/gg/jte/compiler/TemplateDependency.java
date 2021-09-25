package gg.jte.compiler;

public final class TemplateDependency {
    private final String name;
    private final long lastModifiedTimestamp;

    public TemplateDependency(String name, long lastModifiedTimestamp) {
        this.name = name;
        this.lastModifiedTimestamp = lastModifiedTimestamp;
    }

    public String getName() {
        return name;
    }

    public long getLastModifiedTimestamp() {
        return lastModifiedTimestamp;
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
