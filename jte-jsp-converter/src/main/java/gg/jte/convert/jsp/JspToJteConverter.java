package gg.jte.convert.jsp;

import gg.jte.convert.IoUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class JspToJteConverter {
    private final Path jspRoot;
    private final Path jteRoot;
    private final String jteTag;

    private String defaultImports;

    public JspToJteConverter(Path jspRoot, Path jteRoot, String jteTag) {
        this.jspRoot = jspRoot;
        this.jteRoot = jteRoot;
        this.jteTag = jteTag;
    }

    public void setDefaultImports(String defaultImports) {
        this.defaultImports = defaultImports;
    }

    public void convertTag(String jspTag, String jteTag) {
        convertTag(jspRoot.resolve(jspTag), jteRoot.resolve(jteTag));
    }

    private void convertTag(Path jspTag, Path jteTag) {
        JspTagParser parser = new JspTagParser();
        String jte = parser.convert(IoUtils.readFile(jspTag), defaultImports);

        String oldJspTagPrefix = extractTagPrefix(jspTag);
        String newJteFile = jteRoot.relativize(jteTag).toString().replace('\\', '/');

        IoUtils.writeFile(jteTag, jte);
        IoUtils.deleteFile(jspTag);

        try (Stream<Path> stream = Files.walk(jspRoot)) {
            stream
                    .filter(Files::isRegularFile)
                    .filter(p -> !Files.isDirectory(p))
                    .filter(p -> {
                String fileName = p.toString();
                return fileName.endsWith(".jsp") || fileName.endsWith(".jsp.inc") || fileName.endsWith(".tag");
            }).forEach(jspFile -> {
                replaceUsages(jspFile, oldJspTagPrefix, newJteFile);
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void replaceUsages(Path jspFile, String oldJspTagPrefix, String newJteFile) {
        StringBuilder jspContent = new StringBuilder(IoUtils.readFile(jspFile));

        int lastIndex = 0;

        do {
            lastIndex = jspContent.indexOf(oldJspTagPrefix, lastIndex);
            if (lastIndex != -1 && lastIndex < jspContent.length()) {
                if (Character.isWhitespace(jspContent.charAt(lastIndex + oldJspTagPrefix.length()))) {
                    jspContent.replace(lastIndex, lastIndex + oldJspTagPrefix.length(), jteTag + " jte=\"" + newJteFile + "\"");
                }
            }
        } while (lastIndex != -1);

        IoUtils.writeFile(jspFile, jspContent.toString());
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
}
