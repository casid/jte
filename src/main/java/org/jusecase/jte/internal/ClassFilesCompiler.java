package org.jusecase.jte.internal;

import org.jusecase.jte.TemplateException;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.util.List;

public class ClassFilesCompiler {
    public static void compile(String[] files, List<String> compilePath) {
        if (compilePath != null && !compilePath.isEmpty()) {
            String[] args = new String[files.length + 2];
            args[0] = "-classpath";
            args[1] = String.join(File.pathSeparator, compilePath);
            System.arraycopy(files, 0, args, 2, files.length);

            runCompiler(args);
        } else {
            runCompiler(files);
        }
    }

    private static void runCompiler(String[] args) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        int result = compiler.run(null, null, null, args);
        if (result != 0) {
            throw new TemplateException("Java compiler failed with error code " + result + ", failed to compile templates");
        }
    }
}
