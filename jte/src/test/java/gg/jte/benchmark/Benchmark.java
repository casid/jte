package gg.jte.benchmark;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.output.StringOutput;
import gg.jte.resolve.ResourceCodeResolver;

import java.util.concurrent.TimeUnit;

class Benchmark {

    private final TemplateEngine templateEngine;

    public static void main(String[] args) {
        new Benchmark().run();
    }

    Benchmark() {
        templateEngine = TemplateEngine.create(new ResourceCodeResolver("benchmark"), ContentType.Html);
        templateEngine.setTrimControlStructures(true);
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
