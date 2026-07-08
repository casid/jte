package gg.jte;

import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NullMarkedTest {

    @Test
    void rootPackageIsNullMarked() throws ClassNotFoundException {
        Package pkg = Class.forName("gg.jte.generated.precompiled.JtehelloWorldGenerated").getPackage();
        assertThat(pkg.isAnnotationPresent(NullMarked.class)).isTrue();
    }

    @Test
    void tagPackageIsNullMarked() throws ClassNotFoundException {
        Package pkg = Class.forName("gg.jte.generated.precompiled.tag.JtehelloGenerated").getPackage();
        assertThat(pkg.isAnnotationPresent(NullMarked.class)).isTrue();
    }
}
