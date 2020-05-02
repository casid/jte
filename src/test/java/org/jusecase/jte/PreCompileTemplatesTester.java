package org.jusecase.jte;

import org.junit.jupiter.api.Test;
import org.jusecase.jte.internal.TemplateCompiler;
import org.jusecase.jte.resolve.ResourceCodeResolver;

import java.nio.file.Path;
import java.util.List;

public class PreCompileTemplatesTester {
    @Test
    void name() {
        TemplateCompiler compiler = new TemplateCompiler(new ResourceCodeResolver("benchmark"), Path.of("jte"));
        compiler.generateJavaCode(List.of("WelcomePage.jte"));
    }
}
