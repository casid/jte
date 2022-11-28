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
    void lastModified() {
        resourceCodeResolver = new ResourceCodeResolver("benchmark");

        long lastModified = resourceCodeResolver.getLastModified("welcome.jte");

        assertThat(lastModified).isGreaterThan(0L);
    }

    @Test
    void lastModified_templateDoesNotExist() {
        resourceCodeResolver = new ResourceCodeResolver("benchmark");

        long lastModified = resourceCodeResolver.getLastModified("template-that-does-not-exist.jte");

        assertThat(lastModified).isEqualTo(0L);
    }

    @Test
    void listAllTemplateNamesNotSupported() {
        resourceCodeResolver = new ResourceCodeResolver("benchmark");

        Throwable throwable = catchThrowable(() -> resourceCodeResolver.resolveAllTemplateNames());

        assertThat(throwable).isInstanceOf(UnsupportedOperationException.class).hasMessage("This code resolver does not support finding all template names!");
    }

    @Test
    void exists() {
        resourceCodeResolver = new ResourceCodeResolver("benchmark");

        assertThat(resourceCodeResolver.exists("welcome.jte")).isTrue();
        assertThat(resourceCodeResolver.exists("doesNotExist.jte")).isFalse();
    }

    @Test
    void noRootDirectory() {
        resourceCodeResolver = new ResourceCodeResolver("");

        assertThat(resourceCodeResolver.exists("root-template.jte")).isTrue();
        assertThat(resourceCodeResolver.exists("doesNotExist.jte")).isFalse();
    }
}
