package gg.jte.watcher;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.output.StringOutput;
import gg.jte.resolve.DirectoryCodeResolver;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

public class DirectoryWatcherTester {

    @Test
    void run() {
        DirectoryCodeResolver codeResolver = new DirectoryCodeResolver(Paths.get("src", "test", "resources", "watcher"));
        TemplateEngine templateEngine = TemplateEngine.create(codeResolver, Paths.get("jte-classes"), ContentType.Plain);

        DirectoryWatcher watcher = new DirectoryWatcher(templateEngine, codeResolver);
        watcher.startBlocking(templates -> {
            System.out.println("Invalidated " + templates);
            for (String template : templates) {
                templateEngine.prepareForRendering(template);
            }

            StringOutput output = new StringOutput();
            templateEngine.render("main.jte", null, output);
            System.out.println(output);
        });
    }
}
