package gg.jte;

import gg.jte.html.HtmlPolicy;
import gg.jte.html.OwaspHtmlPolicy;
import gg.jte.runtime.Constants;

public class TemplateConfig {
    public static final TemplateConfig PLAIN = new TemplateConfig(ContentType.Plain, Constants.PACKAGE_NAME_PRECOMPILED);

    public final ContentType contentType;
    public final String packageName;
    public String[] compileArgs;
    public boolean trimControlStructures;
    public HtmlPolicy htmlPolicy = new OwaspHtmlPolicy();
    public String[] htmlTags;
    public String[] htmlAttributes;
    public boolean htmlCommentsPreserved;
    public boolean binaryStaticContent;

    public TemplateConfig(ContentType contentType, String packageName) {
        this.contentType = contentType;
        this.packageName = packageName;
    }
}
