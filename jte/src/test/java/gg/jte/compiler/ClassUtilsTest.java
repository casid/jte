package gg.jte.compiler;

import gg.jte.TemplateException;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.net.URLClassLoader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class ClassUtilsTest {
    @Test
    void precompiledTemplatesHint() throws Exception {
        URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{new URL("jar:file:/home/foo/bar/target/foobar.jar!/BOOT-INF/lib/jakarta.annotation-api-1.3.5.jar!/")});

        Throwable throwable = catchThrowable(() -> ClassUtils.resolveClasspathFromClassLoader(classLoader, p -> {
        }));

        assertThat(throwable).isInstanceOf(TemplateException.class).hasMessageContaining("https://github.com/casid/jte/blob/master/DOCUMENTATION.md#using-the-application-class-loader-since-120");
    }
}