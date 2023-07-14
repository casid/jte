package gg.jte.springframework.boot.autoconfigure;

import gg.jte.TemplateEngine;
import gg.jte.output.PrintWriterOutput;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.view.AbstractTemplateView;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

public class JteView extends AbstractTemplateView {

    private final TemplateEngine templateEngine;

    public JteView(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    @Override
    public boolean checkResource(Locale locale) {
        return templateEngine.hasTemplate(this.getUrl());
    }

    @Override
    protected void renderMergedTemplateModel(Map<String, Object> model, javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws Exception {
        String url = this.getUrl();
        response.setContentType(MediaType.TEXT_HTML_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        PrintWriterOutput output = new PrintWriterOutput(response.getWriter());
        templateEngine.render(url, model, output);
    }
}
