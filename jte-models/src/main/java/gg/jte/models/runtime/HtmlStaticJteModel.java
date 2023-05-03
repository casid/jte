package gg.jte.models.runtime;

import gg.jte.html.HtmlInterceptor;
import gg.jte.html.HtmlTemplateOutput;
import gg.jte.html.OwaspHtmlTemplateOutput;
import gg.jte.output.WriterOutput;

import java.io.Writer;

public abstract class HtmlStaticJteModel implements JteModel {
    @Override
    public void render(Writer writer) {
        render(new OwaspHtmlTemplateOutput(new WriterOutput(writer)), null);
    }

    public abstract void render(HtmlTemplateOutput output, HtmlInterceptor interceptor);
}
