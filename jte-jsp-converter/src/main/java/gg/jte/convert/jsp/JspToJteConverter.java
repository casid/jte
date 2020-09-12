package gg.jte.convert.jsp;

import gg.jte.convert.IoUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class JspToJteConverter {
    private final Path jspRoot;
    private final Path jteRoot;
    private final String jteTag;

    public JspToJteConverter(Path jspRoot, Path jteRoot, String jteTag) {
        this.jspRoot = jspRoot;
        this.jteRoot = jteRoot;
        this.jteTag = jteTag;
    }

    public void convertTag(String jspTag, String jteTag, Consumer<JspParser> parserSetup) {
        checkJteName(jteTag);
        convertTag(jspRoot.resolve(jspTag), jteRoot.resolve(jteTag), parserSetup);
    }

    public void replaceUsages(String jspTag, String jteTag) {
        checkJteName(jteTag);
        replaceUsages(jspRoot.resolve(jspTag), jteRoot.resolve(jteTag));
    }

    public void replaceUsages(Path jspTag, Path jteTag) {
        String oldJspTagPrefix = extractTagPrefix(jspTag);
        String newJteFile = jteRoot.relativize(jteTag).toString().replace('\\', '/');

        IoUtils.deleteFile(jspTag);

        try (Stream<Path> stream = Files.walk(jspRoot)) {
            stream
                  .filter(Files::isRegularFile)
                  .filter(p -> !Files.isDirectory(p))
                  .filter(p -> {
                      String fileName = p.toString();
                      return fileName.endsWith(".jsp") || fileName.endsWith(".jsp.inc") || fileName.endsWith(".tag");
                  }).forEach(jspFile -> replaceUsages(jspFile, oldJspTagPrefix, newJteFile));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void convertTag(Path jspTag, Path jteTag, Consumer<JspParser> parserSetup) {
        JspParser parser = new JspParser(this.jteTag);
        if (parserSetup != null) {
            parserSetup.accept(parser);
        }
        String jte = parser.convert(IoUtils.readFile(jspTag));

        System.out.println(jte);

        IoUtils.writeFile(jteTag, jte);

        replaceUsages(jspTag, jteTag);
    }

    private void replaceUsages(Path jspFile, String oldJspTagPrefix, String newJteFile) {
        boolean modified = false;

        StringBuilder jspContent = new StringBuilder(IoUtils.readFile(jspFile));

        int lastIndex = 0;

        do {
            lastIndex = jspContent.indexOf(oldJspTagPrefix, lastIndex);
            if (lastIndex >= 0 && lastIndex < jspContent.length()) {
                char character = jspContent.charAt(lastIndex + oldJspTagPrefix.length());
                if (Character.isWhitespace(character) || character == '/') {
                    jspContent.replace(lastIndex, lastIndex + oldJspTagPrefix.length(), "<" + jteTag + " jte=\"" + newJteFile + "\"");
                    modified = true;
                } else {
                    ++lastIndex;
                }
            } else {
                break;
            }
        } while (true);

        if (modified) {
            IoUtils.writeFile(jspFile, jspContent.toString());
        }
    }

    private String extractTagPrefix(Path jspTag) {
        String fileName = jspTag.getFileName().toString();
        String tagName = fileName.substring(0, fileName.indexOf('.'));

        String namespace = jspTag.getParent().getFileName().toString();
        if ("tags".equals(namespace)) {
            namespace = "include";
        }

        return "<" + namespace + ":" + tagName;
    }

    private void checkJteName(String name) {
        if (name.contains("-")) {
            throw new IllegalArgumentException("Illegal jte tag name '" + name + "'. Tag names should be camel case.");
        }
    }
}
