package gg.jte;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class GenerateClasspathTest {

    @Test
    void generatorIsNotOnRuntimeClasspath() {
        assertThatThrownBy(() -> Class.forName("gg.jte.models.generator.ModelExtension"))
                .isInstanceOf(ClassNotFoundException.class);
    }
}
