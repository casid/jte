package gg.jte.runtime;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClassInfoTest {

    @Test
    void equality() {
        var a1 = new ClassInfo("Message", "my.app");
        var a2 = new ClassInfo("Message", "my.app");
        var b = new ClassInfo("OtherMessage", "my.app");
        var c = new ClassInfo("Message", "my.app.package");

        assertThat(a1).isEqualTo(a2);
        assertThat(a1).hasSameHashCodeAs(a2);

        assertThat(a1).isNotEqualTo(b);
        assertThat(a1).doesNotHaveSameHashCodeAs(b);

        assertThat(a1).isNotEqualTo(c);
        assertThat(a1).doesNotHaveSameHashCodeAs(c);
    }
}