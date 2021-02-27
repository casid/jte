package gg.jte.compiler;

import gg.jte.TemplateConfig;
import gg.jte.runtime.ClassInfo;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface ClassCompiler {
    void compile(String[] files, List<String> compilePath, TemplateConfig config, Path classDirectory, Map<String, ClassInfo> templateByClassName);
}
