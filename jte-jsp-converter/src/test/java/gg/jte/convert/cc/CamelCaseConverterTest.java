package gg.jte.convert.cc;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CamelCaseConverterTest {
    @Test
    void empty() {
        assertConversion("", "");
    }

    @Test
    void kebab() {
        assertConversion("-", "");
        assertConversion("test", "test");
        assertConversion("test-case", "testCase");
        assertConversion("-test-case", "testCase");
        assertConversion("----test----case---", "testCase");
        assertConversion("some-folder/some-cool-tag", "someFolder/someCoolTag");
    }

    @Test
    void snake() {
        assertConversion("_", "");
        assertConversion("test", "test");
        assertConversion("test_case", "testCase");
        assertConversion("_test_case", "testCase");
        assertConversion("____test____case___", "testCase");
        assertConversion("some_folder/some_cool_tag", "someFolder/someCoolTag");
    }

    private void assertConversion(String in, String out) {
        StringBuilder sb = new StringBuilder(in);
        CamelCaseConverter.convertTo(sb);
        assertThat(sb.toString()).isEqualTo(out);
    }
}