package gg.jte.gradle;

import java.nio.file.Path;
import java.util.List;

public class CompilerMojoBase extends JteTaskBase {

    protected List<Path> compilePath;
    protected String htmlPolicyClass;
    protected String[] compileArgs;

    public List<Path> getCompilePath() {
        return compilePath;
    }

    public void setCompilePath(List<Path> value) {
        compilePath = value;
    }

    public String getHtmlPolicyClass() {
        return htmlPolicyClass;
    }

    public void setHtmlPolicyClass(String value) {
        htmlPolicyClass = value;
    }

    public String[] getCompileArgs() {
        return compileArgs;
    }

    public void setCompileArgs(String[] value) {
        compileArgs = value;
    }
}
