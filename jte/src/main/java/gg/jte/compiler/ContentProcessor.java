package gg.jte.compiler;

import java.util.ArrayDeque;
import java.util.Deque;


/**
 * Identifies the various content block regions in a given code segment.
 */
public abstract class ContentProcessor {
   private final int depth;
   private final String code;
   private final Deque<Mode> stack = new ArrayDeque<>();

   private int startIndex = -1;
   private int endIndex = -1;
   private int lastWrittenIndex = -1;
   private Mode currentMode;

   private int i;

   public ContentProcessor(int depth, String code) {
      this.depth = depth;
      this.code = code;
   }

   public void process() {
      push(Mode.Code);

      for ( i = 0; i < code.length(); ++i) {
         if (regionMatches("@`") && currentMode.isContentBlockAllowed()) {
            if (currentMode == Mode.Code) {
               startIndex = i + 1;
            }
            push(Mode.Content);
         } else if (regionMatches("`") && currentMode == Mode.Content) {
            pop();
            if (currentMode == Mode.Code) {
               endIndex = i;
               handleContentBlock();
            }
         } else if (regionMatches("@raw") && currentMode == Mode.Content) {
            push(Mode.Raw);
         } else if (regionMatches("@endraw") && currentMode == Mode.Raw) {
            pop();
         } else if (regionMatches("<%--") && currentMode == Mode.Content) {
            push(Mode.Comment);
         } else if (regionMatches("--%>") && currentMode == Mode.Comment) {
            pop();
         }
      }

      if (lastWrittenIndex + 1 < code.length()) {
         onRemainingCode(code, lastWrittenIndex + 1, code.length());
      }
   }

   private void push(Mode mode) {
      stack.push(mode);
      currentMode = mode;
   }

   private void pop() {
      stack.pop();
      currentMode = stack.peek();
   }

   private boolean regionMatches(String s) {
      return code.regionMatches(i - s.length() + 1, s, 0, s.length());
   }

   private void handleContentBlock() {
      onContentBlock(depth, code, lastWrittenIndex, startIndex, endIndex);

      lastWrittenIndex = endIndex;
   }

   protected abstract void onContentBlock(int depth, String code, int lastWrittenIndex, int startIndex, int endIndex);

   protected abstract void onRemainingCode(String code, int startIndex, int endIndex);

   private enum Mode {
      Code,
      Content,
      Raw,
      Comment,
      ;

      boolean isContentBlockAllowed() {
         return this == Code || this == Content;
      }
   }
}
