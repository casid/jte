package gg.jte;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import gg.jte.output.StringOutput;
import gg.jte.resolve.DirectoryCodeResolver;


public class TemplateEngine_ModulesTest {

   @Test
   void twoModules() {
      DirectoryCodeResolver codeResolver = new DirectoryCodeResolver(Path.of("src/test/modules/two-modules/app"));
      TemplateEngine templateEngine = TemplateEngine.create(codeResolver, ContentType.Html);

      StringOutput output = new StringOutput();
      templateEngine.render("page.jte", null, output);

      assertThat(output.toString().trim()).isEqualToNormalizingNewlines("<style>important</style>\n<p>App Page</p>");
   }

   @Test
   void twoModulesCycle() {
      DirectoryCodeResolver codeResolver = new DirectoryCodeResolver(Path.of("src/test/modules/two-modules-cycle/app"));
      TemplateEngine templateEngine = TemplateEngine.create(codeResolver, ContentType.Html);

      Throwable throwable = catchThrowable(() -> templateEngine.render("page.jte", null, new StringOutput()));

      assertThat(throwable).isInstanceOf(StackOverflowError.class); // TODO better error message :-D
   }

   @Test
   void threeModulesDifferentAlias() {
      DirectoryCodeResolver codeResolver = new DirectoryCodeResolver(Path.of("src/test/modules/three-modules-different-alias/app"));
      TemplateEngine templateEngine = TemplateEngine.create(codeResolver, ContentType.Html);

      StringOutput output = new StringOutput();
      templateEngine.render("page.jte", null, output);

      assertThat(output.toString().trim()).isEqualToNormalizingNewlines("<p>line chart (app)</p>\nline chart (core)");
   }

   @Test
   void threeModulesSameAlias() {
      DirectoryCodeResolver codeResolver = new DirectoryCodeResolver(Path.of("src/test/modules/three-modules-same-alias/app"));
      TemplateEngine templateEngine = TemplateEngine.create(codeResolver, ContentType.Html);

      StringOutput output = new StringOutput();
      templateEngine.render("page.jte", null, output);

      assertThat(output.toString().trim()).isEqualToNormalizingNewlines("<p>line chart (app)</p>\nline chart (core)");
   }

   @Test
   void emptyTopLevelModule() {
      DirectoryCodeResolver codeResolver = new DirectoryCodeResolver(Path.of("src/test/modules/empty-top-level-module"));
      TemplateEngine templateEngine = TemplateEngine.create(codeResolver, ContentType.Html);

      StringOutput output = new StringOutput();
      templateEngine.render("checkout/page.jte", null, output);

      assertThat(output.toString().trim()).isEqualToNormalizingNewlines("""
              <style>important</style>
              <p>line chart (checkout)</p>
              line chart (core)""");
   }

   // TODO adjust precompileAll() and generateAll() to iterate over all module files as well!
}
