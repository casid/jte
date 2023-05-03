package gg.jte.extension;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface JteExtension {
    String name();
    Collection<Path> generate(JteConfig config, Set<TemplateDescription> templateDescriptions);

    default JteExtension init(Map<String, String> value) {
        return this;
    }
}
