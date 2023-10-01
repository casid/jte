package gg.jte.compiler.module;

import java.util.List;


public record ModuleInfo(boolean parent, List<ModuleImport> imports) {}
