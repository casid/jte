package gg.jte.compiler;

import gg.jte.ContentType;
import gg.jte.runtime.TemplateType;

import java.util.Arrays;
import java.util.List;

final class LineInfo {
    private static final List<TemplateParser.Mode> ConditionEndModes = Arrays.asList(TemplateParser.Mode.Condition, TemplateParser.Mode.Text);
    private static final List<TemplateParser.Mode> ForLoopEndModes = Arrays.asList(TemplateParser.Mode.ForLoop, TemplateParser.Mode.Text);

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
                templateParser.setInitialModes(ConditionEndModes);
            } else if (mode == TemplateParser.Mode.ForLoopEnd) {
                templateParser.setInitialModes(ForLoopEndModes);
            }
            templateParser.parse();
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
