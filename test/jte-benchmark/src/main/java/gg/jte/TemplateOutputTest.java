package gg.jte;

import gg.jte.output.StringOutput;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Fork(1)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
public class TemplateOutputTest {

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(TemplateOutputTest.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }

    @Benchmark
    public String templateOutput() {
        TemplateOutput output = new StringOutput();
        JtewelcomeGenerated_TemplateOutput.render(output, null, new WelcomePage(42));
        return output.toString();
    }

    @Benchmark
    public String stringBuilder() {
        StringBuilder output = new StringBuilder(8 * 1024);
        JtewelcomeGenerated_StringBuilder.render(output, null, new WelcomePage(42));
        return output.toString();
    }

    public static class WelcomePage extends Page {
        public WelcomePage(int visits) {
            super(visits);
        }

        @Override
        public String getTitle() {
            return "Welcome!";
        }

        @Override
        public String getDescription() {
            return "Welcome to the benchmark site.";
        }
    }

    public static class MenuItem {
        public final String name;
        public final String url;

        public MenuItem(String name, String url) {
            this.name = name;
            this.url = url;
        }
    }

    public static abstract class Page {
        private final int visits;

        private static final List<MenuItem> MENU = List.of(
                new MenuItem("Home", "home.html"),
                new MenuItem("News", "news.html"),
                new MenuItem("About", "about.html")
        );

        protected Page(int visits) {
            this.visits = visits;
        }

        public abstract String getTitle();

        public abstract String getDescription();

        public List<MenuItem> getMenu() {
            return MENU;
        }

        public int getVisits() {
            return visits;
        }
    }


    @SuppressWarnings({"unused", "Convert2Lambda"})
    public static final class JtewelcomeGenerated_TemplateOutput {
        public static final String JTE_NAME = "welcome.jte";
        public static final int[] JTE_LINE_INFO = {0,0,1,1,1,3,3,3,3,5,5,5};
        public static void render(gg.jte.TemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, WelcomePage page) {
            jteOutput.writeContent("\r\n");
            JtepageGenerated_TemplateOutput.render(jteOutput, jteHtmlInterceptor, page, new gg.jte.Content() {
                public void writeTo(gg.jte.TemplateOutput jteOutput) {
                    jteOutput.writeContent("\r\n    <p>Greetings! Don't forget to visit all the other pages!</p>\r\n");
                }
            });
        }
        public static void renderMap(gg.jte.TemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
            WelcomePage page = (WelcomePage)params.get("page");
            render(jteOutput, jteHtmlInterceptor, page);
        }
    }

    public interface StringBuilderContent {
        void writeTo(StringBuilder jteOutput);
    }

    @SuppressWarnings("unused")
    public static final class JtepageGenerated_TemplateOutput {
        public static final String JTE_NAME = "layout/page.jte";
        public static final int[] JTE_LINE_INFO = {0,0,1,3,3,3,11,11,11,12,12,13,13,14,14,19,19,20,20,20,20,21,21,24,24,25,25,28,28,32};
        public static void render(gg.jte.TemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, Page page, Content content) {
            jteOutput.writeContent("\r\n<!DOCTYPE html>\r\n<html lang=\"en\">\r\n<head>\r\n    <meta charset=\"UTF-8\"/>\r\n    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\r\n");
            if (page.getDescription() != null) {
                jteOutput.writeContent("    <meta name=\"description\" content=\"");
                jteOutput.writeUserContent(page.getDescription());
                jteOutput.writeContent("\">\r\n");
            }
            jteOutput.writeContent("    <title>");
            jteOutput.writeUserContent(page.getTitle());
            jteOutput.writeContent("</title>\r\n</head>\r\n\r\n<body>\r\n    <div class=\"menu\">\r\n");
            for (var menuItem : page.getMenu()) {
                jteOutput.writeContent("        <a href=\"");
                jteOutput.writeUserContent(menuItem.url);
                jteOutput.writeContent("\">");
                jteOutput.writeUserContent(menuItem.name);
                jteOutput.writeContent("</a>\r\n");
            }
            jteOutput.writeContent("    </div>\r\n    <div class=\"content\">\r\n        <h1>");
            jteOutput.writeUserContent(page.getTitle());
            jteOutput.writeContent("</h1>\r\n        ");
            jteOutput.writeUserContent(content);
            jteOutput.writeContent("\r\n    </div>\r\n    <div class=\"footer\">\r\n        This page has ");
            jteOutput.writeUserContent(page.getVisits());
            jteOutput.writeContent(" visits already.\r\n    </div>\r\n</body>\r\n\r\n</html>");
        }
        public static void renderMap(gg.jte.TemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
            Page page = (Page)params.get("page");
            Content content = (Content)params.get("content");
            render(jteOutput, jteHtmlInterceptor, page, content);
        }
    }

    @SuppressWarnings({"unused", "Convert2Lambda"})
    public static final class JtewelcomeGenerated_StringBuilder {
        public static final String JTE_NAME = "welcome.jte";
        public static final int[] JTE_LINE_INFO = {0,0,1,1,1,3,3,3,3,5,5,5};
        public static void render(StringBuilder jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, WelcomePage page) {
            jteOutput.append("\r\n");
            JtepageGenerated_StringBuilder.render(jteOutput, jteHtmlInterceptor, page, new StringBuilderContent() {
                public void writeTo(StringBuilder jteOutput) {
                    jteOutput.append("\r\n    <p>Greetings! Don't forget to visit all the other pages!</p>\r\n");
                }
            });
        }
        public static void renderMap(StringBuilder jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
            WelcomePage page = (WelcomePage)params.get("page");
            render(jteOutput, jteHtmlInterceptor, page);
        }
    }

    @SuppressWarnings("unused")
    public static final class JtepageGenerated_StringBuilder {
        public static final String JTE_NAME = "layout/page.jte";
        public static final int[] JTE_LINE_INFO = {0,0,1,3,3,3,11,11,11,12,12,13,13,14,14,19,19,20,20,20,20,21,21,24,24,25,25,28,28,32};
        public static void render(StringBuilder jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, Page page, StringBuilderContent content) {
            jteOutput.append("\r\n<!DOCTYPE html>\r\n<html lang=\"en\">\r\n<head>\r\n    <meta charset=\"UTF-8\"/>\r\n    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\r\n");
            if (page.getDescription() != null) {
                jteOutput.append("    <meta name=\"description\" content=\"").append(page.getDescription()).append("\">\r\n");
            }
            jteOutput.append("    <title>").append(page.getTitle()).append("</title>\r\n</head>\r\n\r\n<body>\r\n    <div class=\"menu\">\r\n");
            for (var menuItem : page.getMenu()) {
                jteOutput.append("        <a href=\"").append(menuItem.url).append("\">").append(menuItem.name).append("</a>\r\n");
            }
            jteOutput.append("    </div>\r\n    <div class=\"content\">\r\n        <h1>").append(page.getTitle()).append("</h1>\r\n        ");
            content.writeTo(jteOutput);
            jteOutput.append("\r\n    </div>\r\n    <div class=\"footer\">\r\n        This page has ").append(page.getVisits()).append(" visits already.\r\n    </div>\r\n</body>\r\n\r\n</html>");
        }
        public static void renderMap(StringBuilder jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
            Page page = (Page)params.get("page");
            StringBuilderContent content = (StringBuilderContent)params.get("content");
            render(jteOutput, jteHtmlInterceptor, page, content);
        }
    }

}
