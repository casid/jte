package gg.jte.compiler;

import gg.jte.compiler.TemplateCompiler;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TemplateCompiler_DebugModeTest {
    @Test
    void ensureDebugModeIsOff() {
        assertThat(TemplateCompiler.DEBUG).describedAs("Do not deploy debug builds to maven central!").isFalse();
    }
}