package gg.jte.compiler.module;

import gg.jte.CodeResolver;
import gg.jte.TemplateNotFoundException;

import java.util.List;


public class ModuleCodeResolver implements CodeResolver {

   private final CodeResolver codeResolver;
   private final String aliasSlash;

   public ModuleCodeResolver( String alias, CodeResolver codeResolver ) {
      this.codeResolver = codeResolver;
      this.aliasSlash = alias + "/";
   }

   @Override
   public String resolve( String name ) {
      return codeResolver.resolve(removeAlias(name));
   }

   @Override
   public String resolveRequired( String name ) throws TemplateNotFoundException {
      return codeResolver.resolveRequired(removeAlias(name));
   }

   @Override
   public long getLastModified( String name ) {
      return codeResolver.getLastModified(removeAlias(name));
   }

   @Override
   public List<String> resolveAllTemplateNames() {
      return codeResolver.resolveAllTemplateNames();
   }

   @Override
   public boolean exists( String name ) {
      return codeResolver.exists(removeAlias(name));
   }

   private String removeAlias(String name) {
      if (name.startsWith(aliasSlash)) {
         return name.substring(aliasSlash.length());
      }

      return name;
   }
}
