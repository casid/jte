package gg.jte.resolve;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class DirectoryCodeResolverTest {

    DirectoryCodeResolver codeResolver = new DirectoryCodeResolver(Paths.get("src/test/resources/benchmark"));

    @SuppressWarnings("unchecked")
    @Test
    void modificationTimeDoesNotLeak() throws Exception {
        Field modificationTimesField = codeResolver.getClass().getDeclaredField("modificationTimes");
        modificationTimesField.setAccessible(true);
        Map<String, Long> modificationTimes = (Map<String, Long>)modificationTimesField.get(codeResolver);

        codeResolver.resolve("doesNotExist.jte");

        assertThat(modificationTimes).isEmpty();
    }
}
