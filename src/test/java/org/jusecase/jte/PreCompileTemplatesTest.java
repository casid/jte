package org.jusecase.jte;

import org.junit.jupiter.api.Test;
import org.jusecase.jte.resolve.DirectoryCodeResolver;

import java.nio.file.Path;

public class PreCompileTemplatesTest {
    @Test
    void precompileAll() {
        TemplateEngine templateEngine = new TemplateEngine(new DirectoryCodeResolver(Path.of("src/test/resources/benchmark")), Path.of("jte"));
        templateEngine.cleanAll();
        templateEngine.precompileAll();
    }
}
