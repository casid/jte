package org.jusecase.jte.output;

import org.assertj.core.api.Assertions;
import org.jusecase.jte.TemplateOutput;

public class TemplateOutputTest extends AbstractTemplateOutputTest {
    StringBuilder stringBuilder = new StringBuilder();

    @Override
    TemplateOutput createTemplateOutput() {
        return value -> stringBuilder.append(value);
    }

    @Override
    void thenOutputIs(String expected) {
        Assertions.assertThat(stringBuilder.toString()).isEqualTo(expected);
    }
}
