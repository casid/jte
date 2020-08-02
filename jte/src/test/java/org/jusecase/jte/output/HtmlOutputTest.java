package org.jusecase.jte.output;

import org.jusecase.jte.TemplateOutput;
import org.jusecase.jte.html.OwaspHtmlTemplateOutput;

import static org.assertj.core.api.Assertions.assertThat;

public class HtmlOutputTest extends AbstractTemplateOutputTest {

    private StringOutput stringOutput;

    @Override
    TemplateOutput createTemplateOutput() {
        stringOutput = new StringOutput();
        return output = new OwaspHtmlTemplateOutput(stringOutput);
    }

    @Override
    void thenOutputIs(String expected) {
        assertThat(stringOutput.toString()).isEqualTo(expected);
    }
}