package org.jusecase.jte;

import org.junit.jupiter.api.Test;
import org.jusecase.jte.output.StringOutput;

import static org.assertj.core.api.Assertions.assertThat;

public class TemplateEngine_OutputEscapingTest {

    DummyCodeResolver dummyCodeResolver = new DummyCodeResolver();
    TemplateEngine templateEngine = new TemplateEngine(dummyCodeResolver);

    StringOutput stringOutput = new StringOutput();
    SecureOutput secureOutput = new SecureOutput(stringOutput);

    @Test
    void outputEscaping() {
        dummyCodeResolver.givenCode("template.jte", "@import java.lang.String\n" +
                "@param String model\n" +
                "Hello ${model}, ${1}, ${1.0f}");

        templateEngine.render("template.jte", "Model", secureOutput);

        assertThat(stringOutput.toString()).isEqualTo("Hello <cleaned>Model</cleaned>, 1, 1.0");
    }

    @Test
    void alreadyEscaped() {
        dummyCodeResolver.givenCode("template.jte", "@import java.lang.String\n" +
                "@param String model\n" +
                "Hello $safe{model}, ${1}, ${1.0f}");

        templateEngine.render("template.jte", "Model", secureOutput);

        assertThat(stringOutput.toString()).isEqualTo("Hello Model, 1, 1.0");
    }

    static class SecureOutput implements TemplateOutput {

        private final TemplateOutput output;

        SecureOutput(TemplateOutput output) {
            this.output = output;
        }

        @Override
        public void writeSafeContent(String value) {
            output.writeSafeContent(value);
        }

        @Override
        public void writeUnsafeContent(String value) {
            output.writeSafeContent("<cleaned>" + value + "</cleaned>");
        }
    }
}
