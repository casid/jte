package gg.jte.springframework.boot.autoconfigure;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.DirectoryCodeResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.boot.webmvc.autoconfigure.error.ErrorViewResolver;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class JteErrorViewResolverTests {

    private final HttpServletRequest request = new MockHttpServletRequest();
    private final Map<String, Object> model = Map.of("status", 404);
    private final ModelAndView fallbackView = new ModelAndView("fallback");

    @TempDir
    Path tempDir;
    private Path templateDir;
    private Path classDir;
    private Set<String> templates;
    private JteProperties properties;
    private int fallbackCalls;

    @BeforeEach
    void setUp() {
        templateDir = tempDir.resolve("templates");
        classDir = tempDir.resolve("classes");
        templates = Set.of();
        properties = new JteProperties();
        fallbackCalls = 0;
    }

    private TemplateEngine engine() {
        try {
            for (String template : templates) {
                Path file = templateDir.resolve(template);
                Files.createDirectories(file.getParent());
                Files.writeString(file, "error page");
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return TemplateEngine.create(new DirectoryCodeResolver(templateDir), classDir, ContentType.Html, getClass().getClassLoader());
    }

    private JteErrorViewResolver resolver(boolean withFallback) {
        ErrorViewResolver fallback = withFallback ? (r, s, m) -> {
            fallbackCalls++;
            return fallbackView;
        } : null;
        return new JteErrorViewResolver(engine(), properties, fallback);
    }

    @ParameterizedTest
    @CsvSource({"BAD_REQUEST,error/4xx", "NOT_FOUND,error/4xx", "FORBIDDEN,error/4xx",
            "INTERNAL_SERVER_ERROR,error/5xx", "SERVICE_UNAVAILABLE,error/5xx"})
    void seriesTemplateIsUsed(HttpStatus status, String expectedView) {
        templates = Set.of("error/4xx.jte", "error/5xx.jte");

        ModelAndView result = resolver(true).resolveErrorView(request, status, model);

        assertThat(result).isNotNull();
        assertThat(result.getViewName()).isEqualTo(expectedView);
        assertThat(result.getStatus()).isEqualTo(status);
        assertThat(result.getModel()).containsEntry("status", 404);
        assertThat(fallbackCalls).isZero();
    }

    @ParameterizedTest
    @CsvSource({"BAD_REQUEST,400", "UNAUTHORIZED,401", "FORBIDDEN,403", "NOT_FOUND,404", "I_AM_A_TEAPOT,418",
            "TOO_MANY_REQUESTS,429", "INTERNAL_SERVER_ERROR,500", "BAD_GATEWAY,502", "SERVICE_UNAVAILABLE,503"})
    void statusSpecificTemplateWinsOverSeries(HttpStatus status, int code) {
        templates = Set.of("error/" + code + ".jte", "error/4xx.jte", "error/5xx.jte");

        ModelAndView result = resolver(true).resolveErrorView(request, status, model);

        assertThat(result.getViewName()).isEqualTo("error/" + code);
        assertThat(result.getStatus()).isEqualTo(status);
        assertThat(result.getModel()).containsEntry("status", 404);
        assertThat(fallbackCalls).isZero();
    }

    @ParameterizedTest
    @EnumSource(HttpStatus.class)
    void everyStatusCanHaveItsOwnTemplate(HttpStatus status) {
        templates = Set.of("error/" + status.value() + ".jte");

        ModelAndView result = resolver(false).resolveErrorView(request, status, model);

        assertThat(result.getViewName()).isEqualTo("error/" + status.value());
    }

    @Test
    void otherStatusTemplatesAreNotUsed() {
        templates = Set.of("error/500.jte", "error/403.jte");

        assertThat(resolver(true).resolveErrorView(request, HttpStatus.NOT_FOUND, model)).isSameAs(fallbackView);
    }

    @Test
    void statusSpecificTemplateAloneFallsBackToSeriesForOtherStatuses() {
        templates = Set.of("error/404.jte", "error/4xx.jte");

        assertThat(resolver(true).resolveErrorView(request, HttpStatus.NOT_FOUND, model).getViewName()).isEqualTo("error/404");
        assertThat(resolver(true).resolveErrorView(request, HttpStatus.FORBIDDEN, model).getViewName()).isEqualTo("error/4xx");
    }

    @ParameterizedTest
    @CsvSource({"MOVED_PERMANENTLY", "OK", "CONTINUE"})
    void statusesOutsideErrorSeriesIgnoreSeriesTemplates(HttpStatus status) {
        templates = Set.of("error/4xx.jte", "error/5xx.jte");

        assertThat(resolver(true).resolveErrorView(request, status, model)).isSameAs(fallbackView);
        assertThat(resolver(false).resolveErrorView(request, status, model)).isNull();
    }

    @ParameterizedTest
    @CsvSource({"error/5xx.jte,NOT_FOUND", "error/4xx.jte,BAD_GATEWAY"})
    void templateOfOtherSeriesIsIgnored(String template, HttpStatus status) {
        templates = Set.of(template);

        assertThat(resolver(true).resolveErrorView(request, status, model)).isSameAs(fallbackView);
    }

    @Test
    void fallsBackWhenNoTemplateExists() {
        assertThat(resolver(true).resolveErrorView(request, HttpStatus.NOT_FOUND, model)).isSameAs(fallbackView);
        assertThat(fallbackCalls).isEqualTo(1);
    }

    @Test
    void returnsNullWithoutTemplateAndWithoutFallback() {
        assertThat(resolver(false).resolveErrorView(request, HttpStatus.NOT_FOUND, model)).isNull();
    }

    @Test
    void honorsTemplateSuffix() {
        properties.setTemplateSuffix(".html.jte");
        templates = Set.of("error/4xx.jte");
        assertThat(resolver(false).resolveErrorView(request, HttpStatus.NOT_FOUND, model)).isNull();

        templates = Set.of("error/4xx.html.jte");
        assertThat(resolver(false).resolveErrorView(request, HttpStatus.NOT_FOUND, model).getViewName()).isEqualTo("error/4xx");
    }

    @ParameterizedTest
    @CsvSource({"pages/errors,pages/errors/4xx", "/pages/errors/,pages/errors/4xx", "'  errors  ',errors/4xx"})
    void honorsCustomPath(String configured, String expectedView) {
        properties.getErrorView().setPath(configured);
        templates = Set.of(expectedView + ".jte");

        assertThat(resolver(false).resolveErrorView(request, HttpStatus.NOT_FOUND, model).getViewName()).isEqualTo(expectedView);
    }

    @ParameterizedTest
    @CsvSource(value = {"NIL", "''", "/", "'   '"}, nullValues = "NIL")
    void blankPathLooksUpTemplatesAtRoot(String configured) {
        properties.getErrorView().setPath(configured);
        templates = Set.of("4xx.jte");

        assertThat(resolver(false).resolveErrorView(request, HttpStatus.NOT_FOUND, model).getViewName()).isEqualTo("4xx");
    }
}
