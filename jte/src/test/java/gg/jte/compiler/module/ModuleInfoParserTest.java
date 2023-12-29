package gg.jte.compiler.module;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;


class ModuleInfoParserTest {

   @Test
   void empty() {
      ModuleInfo moduleInfo = ModuleInfoParser.parse("");
      assertThat(moduleInfo.imports()).isEmpty();
   }

   @Test
   void oneModule() {
      ModuleInfo moduleInfo = ModuleInfoParser.parse("@import core from ../core");

      assertThat(moduleInfo.imports()).hasSize(1);
      assertThat(moduleInfo.imports().get(0).alias()).isEqualTo("core");
      assertThat(moduleInfo.imports().get(0).from()).isEqualTo("../core");
   }

   @Test
   void twoModules() {
      ModuleInfo moduleInfo = ModuleInfoParser.parse("@import foo from ../foo\n@import bar from ../bar");

      assertThat(moduleInfo.imports()).hasSize(2);
      assertThat(moduleInfo.imports().get(0).alias()).isEqualTo("foo");
      assertThat(moduleInfo.imports().get(0).from()).isEqualTo("../foo");
      assertThat(moduleInfo.imports().get(1).alias()).isEqualTo("bar");
      assertThat(moduleInfo.imports().get(1).from()).isEqualTo("../bar");
   }
}