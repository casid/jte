package org.jusecase.jte;

import org.junit.jupiter.api.Test;
import org.jusecase.jte.internal.TemplateCompiler;
import org.jusecase.jte.resolve.ResourceCodeResolver;

import java.nio.file.Paths;
import java.util.List;

public class PreCompileTemplatesTester {
    @Test
    void name() {
        TemplateCompiler compiler = new TemplateCompiler(new ResourceCodeResolver("benchmark"));
        compiler.generateJavaCode(List.of("WelcomePage.jte"), Paths.get("jte"));
    }
}
