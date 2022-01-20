package gg.jte;

import gg.jte.output.StringOutput;
import gg.jte.resolve.DirectoryCodeResolver;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class TemplateEngine_MigrateV1KeywordsTest {
    StringOutput output = new StringOutput();
    DirectoryCodeResolver codeResolver = new DirectoryCodeResolver(Paths.get("src/test/resources/migrate/v1To2"));
    TemplateEngine templateEngine = TemplateEngine.create(codeResolver, ContentType.Plain);

    @Test
    void oldTag() {
        Throwable throwable = catchThrowable(() -> whenTemplateIsRendered("old-tag-usage.jte"));

        assertThat(throwable).isInstanceOf(TemplateException.class);
        assertThat(throwable.getMessage()).isEqualTo("Failed to compile old-tag-usage.jte, error at line 1: @tag and @layout have been replace with @template since jte 2.\nYour templates must be migrated. You can do this automatically by running the following Java code in your project:\n\n" +
                "public class Migration {\n" +
                "    public static void main(String[] args) {\n" +
                "        gg.jte.migrate.MigrateV1To2.migrateTemplates(java.nio.file.Paths.get(\"" + codeResolver.getRoot().toAbsolutePath().toString().replace("\\", "\\\\") + "\"));\n" +
                "    }\n" +
                "}");
    }

    @Test
    void oldLayout() {
        Throwable throwable = catchThrowable(() -> whenTemplateIsRendered("old-layout-usage.jte"));

        assertThat(throwable).isInstanceOf(TemplateException.class);
        assertThat(throwable.getMessage()).contains("@tag and @layout have been replace with @template since jte 2.");
    }

    private void whenTemplateIsRendered(String name) {
        templateEngine.render(name, null, output);
    }
}
