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

        Utf8ByteOutput output = new Utf8ByteOutput();
        templateEngine.render(url, model, output);

        response.setContentType(MediaType.TEXT_HTML_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentLength(output.getContentLength());

        output.writeTo(response.getOutputStream());
    }

}
