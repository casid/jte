package gg.jte.compiler;

import gg.jte.runtime.ClassInfo;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface ClassCompiler {
    void compile(String[] files, List<String> compilePath, String[] compileArgs, Path classDirectory, Map<String, ClassInfo> templateByClassName);
}
