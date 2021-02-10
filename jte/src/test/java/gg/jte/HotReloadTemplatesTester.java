package gg.jte;

import gg.jte.resolve.DirectoryCodeResolver;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

public class HotReloadTemplatesTester {
    @Test
    void run() {
        DirectoryCodeResolver codeResolver = new DirectoryCodeResolver(Paths.get("src", "test", "resources", "benchmark"));
        TemplateEngine templateEngine = TemplateEngine.create(codeResolver, Paths.get("jte"), ContentType.Plain);

        codeResolver.startTemplateFilesListenerBlocking(templateEngine, templates -> {
            System.out.println("Invalidated " + templates);
            for (String template : templates) {
                templateEngine.prepareForRendering(template);
            }
        });
    }
}
