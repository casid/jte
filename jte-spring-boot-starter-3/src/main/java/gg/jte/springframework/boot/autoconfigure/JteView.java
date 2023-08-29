package gg.jte.springframework.boot.autoconfigure;

import gg.jte.*;
import gg.jte.output.*;
import jakarta.servlet.http.*;
import org.springframework.http.*;
import org.springframework.web.servlet.view.*;

import java.nio.charset.*;
import java.util.*;

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
    protected void renderMergedTemplateModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String url = this.getUrl();
        response.setContentType(MediaType.TEXT_HTML_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        PrintWriterOutput output = new PrintWriterOutput(response.getWriter());
        templateEngine.render(url, model, output);
    }

}
