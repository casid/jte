package gg.jte.springframework.boot.autoconfigure;

import gg.jte.TemplateEngine;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.webmvc.autoconfigure.error.ErrorViewResolver;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

/**
 * Resolves error pages from jte templates: first a template for the exact status ({@code <path>/<status>},
 * e.g. {@code error/404}, {@code error/418}, {@code error/503}), then {@code <path>/4xx} for client errors or
 * {@code <path>/5xx} for server errors. Anything else goes to the fallback resolver.
 */
public class JteErrorViewResolver implements ErrorViewResolver {

    private final TemplateEngine templateEngine;
    private final String suffix;
    private final String path;
    private final ErrorViewResolver fallback;

    public JteErrorViewResolver(TemplateEngine templateEngine, JteProperties jteProperties, ErrorViewResolver fallback) {
        this.templateEngine = templateEngine;
        this.suffix = jteProperties.getTemplateSuffix();
        this.path = normalize(jteProperties.getErrorView().getPath());
        this.fallback = fallback;
    }

    @Override
    public ModelAndView resolveErrorView(HttpServletRequest request, HttpStatus status, Map<String, Object> model) {
        String specific = path + status.value();
        if (templateEngine.hasTemplate(specific + suffix)) {
            return new ModelAndView(specific, model, status);
        }
        String series = status.is4xxClientError() ? path + "4xx" : status.is5xxServerError() ? path + "5xx" : null;
        if (series != null && templateEngine.hasTemplate(series + suffix)) {
            return new ModelAndView(series, model, status);
        }
        return fallback == null ? null : fallback.resolveErrorView(request, status, model);
    }

    private static String normalize(String configured) {
        String trimmed = configured == null ? "" : configured.strip().replaceAll("^/+|/+$", "");
        return trimmed.isEmpty() ? "" : trimmed + "/";
    }
}
