package gg.jte.compiler;

import java.util.List;

public interface CodeGenerator extends TemplateParserVisitor {
    String getCode();

    List<byte[]> getBinaryTextParts();
}
