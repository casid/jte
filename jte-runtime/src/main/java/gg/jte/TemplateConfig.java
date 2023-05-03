package gg.jte;

import gg.jte.html.HtmlPolicy;
import gg.jte.html.OwaspHtmlPolicy;
import gg.jte.runtime.Constants;

import java.nio.file.Path;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TemplateConfig {
    public static final TemplateConfig PLAIN = new TemplateConfig(ContentType.Plain, Constants.PACKAGE_NAME_PRECOMPILED);

    public final ContentType contentType;
    public final String packageName;
    public String[] compileArgs;
    public boolean trimControlStructures;
    public HtmlPolicy htmlPolicy = new OwaspHtmlPolicy();
    public String[] htmlTags;
    public boolean htmlCommentsPreserved;
    public boolean binaryStaticContent;
    public List<String> classPath;

    public Path resourceDirectory;

    public String projectNamespace;

    /**
     * @deprecated moved to extension
     */
    @Deprecated
    public boolean generateNativeImageResources;
    public Map<String, Map<String, String>> extensionClasses = new HashMap<>();

    public TemplateConfig(ContentType contentType, String packageName) {
        this.contentType = contentType;
        this.packageName = packageName;
    }
}
