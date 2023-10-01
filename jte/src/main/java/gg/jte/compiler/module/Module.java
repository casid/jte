package gg.jte.compiler.module;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import gg.jte.CodeResolver;


public final class Module {

   public static String getModuleAlias(String name) {
      int index = name.indexOf('/');
      if (index == -1) {
         return null;
      }

      return name.substring(0, index);
   }

   private final String alias;
   private final CodeResolver codeResolver;
   private final Map<String, Module> children;
   private final boolean parent;

   public Module( String alias, CodeResolver codeResolver, Map<String, Module> children, boolean parent ) {
      this.alias = alias;
      this.codeResolver = isRoot() ? codeResolver : new ModuleCodeResolver(alias, codeResolver);
      this.children = children;
      this.parent = parent;
   }

   public Module resolve(String name) {
      if (children.isEmpty()) {
         return this;
      }

      String moduleAlias = getModuleAlias(name);
      if (moduleAlias == null) {
         return this;
      }

      Module result = children.get(moduleAlias);
      if (result == null) {
         return this;
      }

      return result;
   }

   public String normalize(String name) {
      if (isRoot()) {
         return name;
      } else {
         if (name.startsWith(alias + "/")) {
            return name;
         } else {
            return alias + "/" + name;
         }
      }
   }

   public boolean isRoot() {
      return alias.isEmpty();
   }

   public CodeResolver getCodeResolver() {
      return codeResolver;
   }

   public Collection<String> resolveAllTemplateNames() {
      LinkedHashSet<String> result = new LinkedHashSet<>();
      resolveAllTemplateNames(result);
      return result;
   }

   private void resolveAllTemplateNames(LinkedHashSet<String> result) {
      if (!parent) {
         List<String> names = codeResolver.resolveAllTemplateNames();
         for (String name : names) {
            result.add(normalize(name));
         }
      }

      for (Module module : children.values()) {
         module.resolveAllTemplateNames(result);
      }
   }
}
