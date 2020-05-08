package org.jusecase.jte;

import org.junit.jupiter.api.Test;
import org.jusecase.jte.resolve.DirectoryCodeResolver;

import java.nio.file.Path;

public class HotReloadTemplatesTester {
    @Test
    void name() {
        DirectoryCodeResolver codeResolver = new DirectoryCodeResolver(Path.of("src", "test", "resources", "benchmark"));
        TemplateEngine templateEngine = new TemplateEngine(codeResolver, Path.of("jte"));

        codeResolver.enableHotReloadBlocking(templateEngine, templates -> {
            System.out.println("Invalidated " + templates);
            for (String template : templates) {
                templateEngine.prepareForRendering(template);
            }
        });
    }
}
