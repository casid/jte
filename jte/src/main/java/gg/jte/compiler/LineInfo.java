package gg.jte.compiler;

import gg.jte.ContentType;
import gg.jte.runtime.TemplateType;

import java.util.List;

final class LineInfo {
    static boolean isSingleControlStructure(String templateCode, int currentIndex, int endIndex, int startLineIndex, TemplateParser.Mode mode) {
        int endLineIndex = templateCode.indexOf('\n', currentIndex);
        if (endLineIndex == -1) {
            endLineIndex = endIndex;
        }

        try {
            TemplateParser templateParser = new TemplateParser(templateCode, TemplateType.Template, new TemplateSingleControlStructureVisitor(), ContentType.Plain, null, null, null, false, false);
            templateParser.setStartIndex(startLineIndex);
            templateParser.setEndIndex(endLineIndex);
            if (mode == TemplateParser.Mode.ConditionEnd) {
                templateParser.setInitialModes(List.of(TemplateParser.Mode.Condition, TemplateParser.Mode.Text));
            } else if (mode == TemplateParser.Mode.ForLoopEnd) {
                templateParser.setInitialModes(List.of(TemplateParser.Mode.ForLoop, TemplateParser.Mode.Text));
            }
            templateParser.parse();
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
