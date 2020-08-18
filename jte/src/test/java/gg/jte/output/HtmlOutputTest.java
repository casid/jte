package gg.jte.output;

import gg.jte.TemplateOutput;
import gg.jte.html.OwaspHtmlTemplateOutput;

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