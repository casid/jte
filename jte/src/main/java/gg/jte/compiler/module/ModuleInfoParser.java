package gg.jte.compiler.module;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;


public class ModuleInfoParser {

   public static ModuleInfo parse(String moduleInfo) {
      return new ModuleInfoParser(moduleInfo).parse();
   }

   private final String moduleInfo;
   private final int startIndex;
   private final int endIndex;

   private final Deque<Mode> stack = new ArrayDeque<>();

   private int i;
   private int lastIndex = 0;
   private Mode currentMode;

   private ModuleInfoParser( String moduleInfo ) {
      this.moduleInfo = moduleInfo;

      this.startIndex = 0;
      this.endIndex = moduleInfo.length();
   }

   private ModuleInfo parse() {
      boolean parent = false;
      List<ModuleImport> imports = new ArrayList<>();

      push(Mode.Root);

      for ( i = startIndex; i < endIndex; ++i) {
         char currentChar = moduleInfo.charAt(i);

         if (currentMode == Mode.Root && regionMatches("@import")) {
            push(new ImportMode());
            lastIndex = i + 1;
         } else if (currentMode instanceof ImportMode importMode) {
            if (regionMatches("from")) {
               importMode.alias = extractFromLastIndex(-4).trim();
            } else if ( currentChar == '\n' || i == endIndex - 1) {
               importMode.from = extractFromLastIndex(1).trim();
               imports.add(new ModuleImport(importMode.alias, importMode.from));
               pop();
            }
         } else if (currentMode == Mode.Root && regionMatches("@parent")) {
            parent = true;
            lastIndex = i + 1;
         }
      }

      return new ModuleInfo(parent, imports);
   }

   private String extractFromLastIndex(int offset) {
      String result = moduleInfo.substring(lastIndex, i + offset);
      lastIndex = i + 1;
      return result;
   }

   private void push( Mode mode) {
      currentMode = mode;
      stack.push(currentMode);
   }

   private void pop() {
      stack.pop();
      currentMode = stack.peek();
   }

   private boolean regionMatches(String s) {
      return moduleInfo.regionMatches(i - s.length() + 1, s, 0, s.length());
   }

   private interface Mode {
      Mode Root = new StatelessMode("Root");
   }

   private static class StatelessMode implements Mode {

      private final String debugName;

      public StatelessMode( String debugName ) {
         this.debugName = debugName;
      }

      @Override
      public String toString() {
         return getClass().getSimpleName() + "[" + debugName + "]";
      }
   }

   private static class ImportMode implements Mode {
      String alias;
      String from;
   }
}
