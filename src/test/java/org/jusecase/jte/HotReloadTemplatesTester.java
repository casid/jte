package org.jusecase.jte;

import org.junit.jupiter.api.Test;
import org.jusecase.jte.resolve.DirectoryCodeResolver;

import java.nio.file.Path;

public class HotReloadTemplatesTester {
    @Test
    void name() {
        DirectoryCodeResolver codeResolver = new DirectoryCodeResolver(Path.of("src", "test", "resources", "benchmark"));
        TemplateEngine templateEngine = new TemplateEngine(codeResolver, Path.of("jte"));

        codeResolver.enableHotReloadBlocking(templateEngine, s -> System.out.println("Invalidated " + s));
    }
}
