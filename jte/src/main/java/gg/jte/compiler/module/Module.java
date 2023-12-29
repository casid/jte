package gg.jte.compiler.module;

import java.nio.file.Path;
import java.util.*;

import gg.jte.CodeResolver;
import gg.jte.resolve.DirectoryCodeResolver;


public final class Module {

   public static Module create(String alias, CodeResolver codeResolver) {
      String jteRootContent = codeResolver.resolve(".jteroot");
      if (jteRootContent == null) {
         return new Module(alias, codeResolver, Map.of(), false);
      }

      if (!(codeResolver instanceof DirectoryCodeResolver directoryCodeResolver)) {
         return new Module(alias, codeResolver, Map.of(), false);
      }

      ModuleInfo moduleInfo = ModuleInfoParser.parse(jteRootContent);
      Map<String, Module> children = new LinkedHashMap<>();

      for ( ModuleImport moduleImport : moduleInfo.imports() ) {
         Path modulePath = directoryCodeResolver.getRoot().resolve(moduleImport.from()).normalize();
         DirectoryCodeResolver moduleDirectoryResolver = new DirectoryCodeResolver(modulePath);
         children.put(moduleImport.alias(), create(moduleImport.alias(), moduleDirectoryResolver));
      }

      return new Module(alias, codeResolver, children, moduleInfo.parent());
   }

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
