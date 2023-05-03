package gg.jte.models.runtime;

import gg.jte.TemplateOutput;
import gg.jte.html.HtmlInterceptor;
import gg.jte.output.WriterOutput;

import java.io.Writer;

public abstract class PlainStaticJteModel implements JteModel {
    @Override
    public void render(Writer writer) {
        render(new WriterOutput(writer), null);
    }

    public abstract void render(TemplateOutput output, HtmlInterceptor interceptor);
}
