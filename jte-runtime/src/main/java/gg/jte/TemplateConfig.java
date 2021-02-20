package gg.jte;

import gg.jte.html.HtmlPolicy;
import gg.jte.html.OwaspHtmlPolicy;

public class TemplateConfig {
    public static final TemplateConfig PLAIN = new TemplateConfig(ContentType.Plain);

    public final ContentType contentType;
    public String[] compileArgs;
    public boolean trimControlStructures;
    public HtmlPolicy htmlPolicy = new OwaspHtmlPolicy();
    public String[] htmlTags;
    public String[] htmlAttributes;
    public boolean htmlCommentsPreserved;
    public boolean binaryStaticContent;

    public TemplateConfig(ContentType contentType) {
        this.contentType = contentType;
    }
}
