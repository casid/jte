package gg.jte.resolve;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.List;

public class CompositeCodeResolverTest {

    CompositeCodeResolver compositeCodeResolver;

    @Test
    void resolveTemplate() {
        ResourceCodeResolver resourceCodeResolver = new ResourceCodeResolver("benchmark");
        DirectoryCodeResolver directoryCodeResolver = new DirectoryCodeResolver(Paths.get("src/test/resources/benchmark"));
        compositeCodeResolver = new CompositeCodeResolver(
                List.of(resourceCodeResolver, directoryCodeResolver)
        );

        String welcomeTemplateResolve = compositeCodeResolver.resolve("welcome.jte");
        String pageTemplateResolve = compositeCodeResolver.resolve("layout/page.jte");

        Assertions.assertNotNull(welcomeTemplateResolve);
        Assertions.assertNotNull(pageTemplateResolve);
    }


    @Test
    void getLastModifiedTemplate() {
        ResourceCodeResolver resourceCodeResolver = new ResourceCodeResolver("benchmark");
        DirectoryCodeResolver directoryCodeResolver = new DirectoryCodeResolver(Paths.get("src/test/resources/benchmark"));
        compositeCodeResolver = new CompositeCodeResolver(
                List.of(resourceCodeResolver, directoryCodeResolver)
        );

        long welcomeTemplateLastModified = compositeCodeResolver.getLastModified("welcome.jte");
        long pageTemplateLastModified = compositeCodeResolver.getLastModified("layout/page.jte");

        Assertions.assertTrue(welcomeTemplateLastModified > 0);
        Assertions.assertTrue(pageTemplateLastModified > 0);
    }

    @Test
    void resolveAllTemplateNames() {
        ResourceCodeResolver resourceCodeResolver = new ResourceCodeResolver("benchmark");
        DirectoryCodeResolver directoryCodeResolver = new DirectoryCodeResolver(Paths.get("src/test/resources/benchmark"));
        compositeCodeResolver = new CompositeCodeResolver(
                List.of(resourceCodeResolver, directoryCodeResolver)
        );

        List<String> allTemplateNames = compositeCodeResolver.resolveAllTemplateNames();

        Assertions.assertTrue(allTemplateNames.contains("welcome.jte"));
        Assertions.assertTrue(allTemplateNames.contains("layout/page.jte"));
    }
}
