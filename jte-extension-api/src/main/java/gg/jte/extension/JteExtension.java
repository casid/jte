package gg.jte.extension;

import java.util.Set;

public interface JteExtension {
    void generate(JteConfig config, Set<? extends JteClassDefinition> classDefinitions);
}
