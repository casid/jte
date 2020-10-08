package gg.jte.resolve;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class ResourceCodeResolverTest {
    ResourceCodeResolver resourceCodeResolver;

    @Test
    void customClassLoader() {
        resourceCodeResolver = new ResourceCodeResolver("benchmark", getClass().getClassLoader());

        String code = resourceCodeResolver.resolve("welcome.jte");

        assertThat(code).isNotBlank();
    }

    @Test
    void contextClassLoader() {
        resourceCodeResolver = new ResourceCodeResolver("benchmark");

        String code = resourceCodeResolver.resolve("welcome.jte");

        assertThat(code).isNotBlank();
    }

    @Test
    void notFound() {
        resourceCodeResolver = new ResourceCodeResolver("benchmark");

        String code = resourceCodeResolver.resolve("does-not-exist.jte");

        assertThat(code).isNull();
    }

    @Test
    void changesNotSupported() {
        resourceCodeResolver = new ResourceCodeResolver("benchmark");

        boolean hasChanged = resourceCodeResolver.hasChanged("welcome.jte");

        assertThat(hasChanged).isFalse();
    }

    @Test
    void listAllTemplateNamesNotSupported() {
        resourceCodeResolver = new ResourceCodeResolver("benchmark");

        Throwable throwable = catchThrowable(() -> resourceCodeResolver.resolveAllTemplateNames());

        assertThat(throwable).isInstanceOf(UnsupportedOperationException.class).hasMessage("This code resolver does not support finding all template names!");
    }
}