package gg.jte.compiler;

import gg.jte.ContentType;
import gg.jte.DummyCodeResolver;
import gg.jte.TemplateConfig;
import gg.jte.TemplateException;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class TemplateCompiler_KotlinNotFoundTest {
    @Test
    void kotlinNotOnClassPath() {
        TemplateCompiler templateCompiler = new TemplateCompiler(new TemplateConfig(ContentType.Plain), new DummyCodeResolver(), Paths.get(""), null);

        Throwable throwable = catchThrowable(() -> templateCompiler.createCompiler("kt"));

        assertThat(throwable).isInstanceOf(TemplateException.class).hasMessage("Failed to create kotlin compiler. To compile .kte files, you need to add gg.jte:jte-kotlin to your project.");
    }
}