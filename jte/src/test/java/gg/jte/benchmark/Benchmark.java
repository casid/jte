package gg.jte.benchmark;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.output.StringOutput;
import gg.jte.resolve.DirectoryCodeResolver;
import gg.jte.runtime.Constants;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

class Benchmark {

    public static Path getTemplateDirectory() {
        URL res = Benchmark.class.getClassLoader().getResource("benchmark/welcome.jte");
        if (res == null) {
            throw new IllegalStateException("Resource benchmark/welcome.jte not found!");
        }

        try {
            return new File(res.toURI()).toPath().getParent();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private final TemplateEngine templateEngine;

    public static void main(String[] args) {
        new Benchmark().run();
    }

    Benchmark() {
        Path classDirectory = Paths.get("jte-classes");

        TemplateEngine compiler = TemplateEngine.create(new DirectoryCodeResolver(getTemplateDirectory()), classDirectory, ContentType.Html, null, Constants.PACKAGE_NAME_PRECOMPILED);
        compiler.setTrimControlStructures(true);
        compiler.precompileAll();

        templateEngine = TemplateEngine.createPrecompiled(classDirectory, ContentType.Html);
    }

    public void run() {
        System.out.println("Rendering welcome page for the first time, this will cause the template to compile.");
        renderWelcomePage(1);

        System.out.println("Rendering welcome page a million times.");
        renderWelcomePage(1_000_000);
    }

    private void renderWelcomePage(int amount) {
        long start = System.nanoTime();

        for (int i = 0; i < amount; ++i) {
            Page page = new WelcomePage(i);
            render(page);
        }

        long end = System.nanoTime();

        System.out.println(amount + " pages rendered in " + TimeUnit.NANOSECONDS.toMillis(end - start) + "ms." + " (~ " + ((float)TimeUnit.NANOSECONDS.toMicros(end - start) / amount) + "µs per page)");
        System.out.println();
    }

    StringOutput render(Page page) {
        StringOutput output = new StringOutput();
        templateEngine.render(page.getTemplate(), page, output);
        return output;
    }
}
