package gg.jte.runtime;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static gg.jte.runtime.StringUtils.startsWithIgnoringCaseAndWhitespaces;

class StringUtilsTest {
    @Test
    void testStartsWithIgnoringCaseAndWhitespaces() {
        String prefix = "javascript:";

        Assertions.assertThat(startsWithIgnoringCaseAndWhitespaces("foo", prefix)).isFalse();
        Assertions.assertThat(startsWithIgnoringCaseAndWhitespaces("javascript:123", prefix)).isTrue();
        Assertions.assertThat(startsWithIgnoringCaseAndWhitespaces("j", prefix)).isFalse();
        Assertions.assertThat(startsWithIgnoringCaseAndWhitespaces("javascript", prefix)).isFalse();
        Assertions.assertThat(startsWithIgnoringCaseAndWhitespaces(prefix, prefix)).isTrue();
        Assertions.assertThat(startsWithIgnoringCaseAndWhitespaces("   javascript:", prefix)).isTrue();
        Assertions.assertThat(startsWithIgnoringCaseAndWhitespaces("  \t JavaScript:", prefix)).isTrue();
        Assertions.assertThat(startsWithIgnoringCaseAndWhitespaces("  \t JavaScript", prefix)).isFalse();
        Assertions.assertThat(startsWithIgnoringCaseAndWhitespaces("java\tscript:", prefix)).isTrue();
        Assertions.assertThat(startsWithIgnoringCaseAndWhitespaces("java\nscript:", prefix)).isTrue();
        Assertions.assertThat(startsWithIgnoringCaseAndWhitespaces("java\rscript:", prefix)).isTrue();
        Assertions.assertThat(startsWithIgnoringCaseAndWhitespaces("java script:", prefix)).isFalse();
        Assertions.assertThat(startsWithIgnoringCaseAndWhitespaces("java\0script:", prefix)).isFalse();

        for (char c = 0; c <= ' '; ++c) {
            Assertions.assertThat(startsWithIgnoringCaseAndWhitespaces(c + prefix, prefix)).isTrue();
        }
    }
}
