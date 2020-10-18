package gg.jte.internal;

import gg.jte.ContentType;
import gg.jte.internal.TemplateParser.Mode;

import java.util.List;

final class LineInfo {
    static boolean isSingleControlStructure(String templateCode, int currentIndex, int endIndex, int startLineIndex, Mode mode) {
        int endLineIndex = templateCode.indexOf('\n', currentIndex);
        if (endLineIndex == -1) {
            endLineIndex = endIndex;
        }

        try {
            TemplateParser templateParser = new TemplateParser(templateCode, TemplateType.Template, new TemplateSingleControlStructureVisitor(), ContentType.Plain, null, null, null, false);
            templateParser.setStartIndex(startLineIndex);
            templateParser.setEndIndex(endLineIndex);
            if (mode == Mode.ConditionEnd) {
                templateParser.setInitialModes(List.of(Mode.Condition, Mode.Text));
            } else if (mode == Mode.ForLoopEnd) {
                templateParser.setInitialModes(List.of(Mode.ForLoop, Mode.Text));
            }
            templateParser.parse();
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
