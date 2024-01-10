package gg.jte.compiler.extensionsupport;

import gg.jte.compiler.ClassDefinition;
import gg.jte.runtime.ClassInfo;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExtensionTemplateDescriptionTest {

    @Test
    void equality() {
        var a1 = new ExtensionTemplateDescription(
                new ClassDefinition("Stub", "kt"),
                new ClassInfo("Message", "my.app")
        );
        var a2 = new ExtensionTemplateDescription(
                new ClassDefinition("Stub", "kt"),
                new ClassInfo("Message", "my.app")
        );
        var b = new ExtensionTemplateDescription(
                new ClassDefinition("Stub", "kt"),
                new ClassInfo("OtherMessage", "my.app")
        );
        var c = new ExtensionTemplateDescription(
                new ClassDefinition("OtherStub", "kt"),
                new ClassInfo("Message", "my.app")
        );
        var d = new ExtensionTemplateDescription(
                null,
                new ClassInfo("Message", "my.app")
        );
        var e = new ExtensionTemplateDescription(
                new ClassDefinition("OtherStub", "kt"),
                null
        );

        assertThat(a1).isEqualTo(a2);
        assertThat(a1).hasSameHashCodeAs(a2);

        assertThat(a1).isNotEqualTo(b);
        assertThat(a1).doesNotHaveSameHashCodeAs(b);

        assertThat(a1).isNotEqualTo(c);
        assertThat(a1).doesNotHaveSameHashCodeAs(c);

        assertThat(a1).isNotEqualTo(d);
        assertThat(a1).doesNotHaveSameHashCodeAs(d);

        assertThat(a1).isNotEqualTo(e);
        assertThat(a1).doesNotHaveSameHashCodeAs(e);
    }
}