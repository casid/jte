package gg.jte.compiler;

import java.util.Objects;

public final class TemplateDependency {
    private final TemplateDependencyShared shared;
    private long lastLoadedTimestamp;

    public TemplateDependency(TemplateDependencyShared shared) {
        this.shared = shared;
    }

    public String getName() {
        return shared.getName();
    }

    public long getLastLoadedTimestamp() {
        return lastLoadedTimestamp;
    }

    public void setLastLoadedTimestamp(long lastLoadedTimestamp) {
        this.lastLoadedTimestamp = lastLoadedTimestamp;
    }

    public long getLastCompiledTimestamp() {
        return shared.getLastCompiledTimestamp();
    }

    public void setLastCompiledTimestamp(long lastCompiledTimestamp) {
        shared.setLastCompiledTimestamp(lastCompiledTimestamp);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TemplateDependency that = (TemplateDependency) o;
        return shared.equals(that.shared);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shared);
    }
}
