package gg.jte.springframework.boot.autoconfigure;

import gg.jte.TemplateEngine;
import gg.jte.output.PrintWriterOutput;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
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
    public boolean checkResource(@NonNull Locale locale) {
        return templateEngine.hasTemplate(this.getUrl());
    }

    @Override
    protected void renderMergedTemplateModel(@NonNull Map<String, Object> model, @NonNull HttpServletRequest request, HttpServletResponse response) throws Exception {
        String url = this.getUrl();
        response.setContentType(MediaType.TEXT_HTML_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        PrintWriterOutput output = new PrintWriterOutput(response.getWriter());
        templateEngine.render(url, model, output);
    }
}
