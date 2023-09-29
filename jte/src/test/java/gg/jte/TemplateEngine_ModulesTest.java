package gg.jte;

import static org.assertj.core.api.Assertions.assertThat;

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
   void threeModules() {
      DirectoryCodeResolver codeResolver = new DirectoryCodeResolver(Path.of("src/test/modules/three-modules/app"));
      TemplateEngine templateEngine = TemplateEngine.create(codeResolver, ContentType.Html);

      StringOutput output = new StringOutput();
      templateEngine.render("page.jte", null, output);

      assertThat(output.toString().trim()).isEqualToNormalizingNewlines("<p>line chart (app)</p>\nline chart (core)");
   }

   // TODO adjust precompileAll() and generateAll() to iterate over all module files as well!
}
