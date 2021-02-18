package gg.jte.output;

import static org.assertj.core.api.Assertions.assertThat;

public class StringOutputTest extends AbstractTemplateOutputTest<StringOutput> {
    @Override
    StringOutput createTemplateOutput() {
        return new StringOutput();
    }

    @Override
    void thenOutputIs(String expected) {
        assertThat(output.toString()).isEqualTo(expected);
    }
}