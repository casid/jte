package gg.jte.benchmark;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.output.Utf8ByteOutput;
import gg.jte.resolve.DirectoryCodeResolver;
import gg.jte.runtime.Constants;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

class Benchmark_Binary {

    private final TemplateEngine templateEngine;

    public static void main(String[] args) {
        new Benchmark_Binary().run();
    }

    Benchmark_Binary() {
        Path classDirectory = Paths.get("jte-classes");

        TemplateEngine compiler = TemplateEngine.create(new DirectoryCodeResolver(Benchmark.getTemplateDirectory()), classDirectory, ContentType.Html, null, Constants.PACKAGE_NAME_PRECOMPILED);
        compiler.setTrimControlStructures(true);
        compiler.setBinaryStaticContent(true);
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

    Utf8ByteOutput render(Page page) {
        Utf8ByteOutput output = new Utf8ByteOutput();
        templateEngine.render(page.getTemplate(), page, output);
        return output;
    }
}
