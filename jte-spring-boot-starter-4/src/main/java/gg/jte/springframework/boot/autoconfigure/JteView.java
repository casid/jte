package gg.jte.springframework.boot.autoconfigure;

import gg.jte.TemplateEngine;
import gg.jte.TemplateNotFoundException;
import gg.jte.output.PrintWriterOutput;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
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
        String url = this.getUrl();
        try {
            templateEngine.prepareForRendering(url);
            return true;
        } catch (TemplateNotFoundException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("jte template [" + url + "] not found", e);
            }
            return false;
        }
    }

    @Override
    protected void renderMergedTemplateModel(@NonNull Map<String, Object> model, @NonNull HttpServletRequest request, HttpServletResponse response) throws Exception {
        String url = this.getUrl();
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        PrintWriterOutput output = new PrintWriterOutput(response.getWriter());
        templateEngine.render(url, model, output);
    }
}
