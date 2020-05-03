package org.jusecase.jte;

import org.junit.jupiter.api.Test;
import org.jusecase.jte.internal.TemplateCompiler;
import org.jusecase.jte.resolve.ResourceCodeResolver;

import java.nio.file.Path;

public class PreCompileTemplatesTester {
    @Test
    void name() {
        TemplateCompiler compiler = new TemplateCompiler(new ResourceCodeResolver("benchmark"), Path.of("jte"));
        compiler.precompileAll(Path.of("src", "test", "resources", "benchmark"));
    }
}
