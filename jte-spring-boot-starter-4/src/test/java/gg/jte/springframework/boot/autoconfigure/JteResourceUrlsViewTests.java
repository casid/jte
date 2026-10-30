package gg.jte.springframework.boot.autoconfigure;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.DirectoryCodeResolver;
import gg.jte.runtime.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;
import org.springframework.web.servlet.resource.ResourceUrlProvider;
import org.springframework.web.servlet.resource.ResourceUrlProviderExposingInterceptor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class JteResourceUrlsViewTests {

    @TempDir
    Path classes;

    TemplateEngine engine;

    @BeforeEach
    void createEngine() {
        engine = TemplateEngine.create(new DirectoryCodeResolver(Path.of("src/test/jte")), classes, ContentType.Html);
    }

    @Test
    void helperWorksInLayoutWithoutMutatingModel() throws Exception {
        Map<String, Object> model = Map.of("resourcePath", "/css/bundle.css?a=1&b=\"quoted\"#style");

        String html = render(view(engine, "resources.jte"), model, new MockHttpServletRequest());

        assertThat(html).contains("href=\"/css/bundle.css?a=1&amp;b=&#34;quoted&#34;#style\"");
        assertThat(model).containsOnlyKeys("resourcePath");
    }

    @Test
    void preservesExistingModelAttribute() throws Exception {
        Map<String, Object> model = Map.of("resources", "application value");

        assertThat(render(view(engine, "resource-collision.jte"), model, new MockHttpServletRequest()))
                .contains("application value");
        assertThat(model).containsEntry("resources", "application value");
    }

    @Test
    void preservesExplicitNullModelAttribute() throws Exception {
        Map<String, Object> model = new HashMap<>();
        model.put("resources", null);

        assertThat(render(view(engine, "resource-collision.jte"), model, new MockHttpServletRequest()).trim())
                .isEmpty();
        assertThat(model).containsEntry("resources", null);
    }

    @Test
    void helperWorksWithPrecompiledTemplates() throws Exception {
        TemplateEngine precompiler = TemplateEngine.create(new DirectoryCodeResolver(Path.of("src/test/jte")),
                classes, ContentType.Html, null, Constants.PACKAGE_NAME_PRECOMPILED);
        precompiler.precompileAll();
        TemplateEngine precompiled = TemplateEngine.createPrecompiled(classes, ContentType.Html);

        assertThat(render(view(precompiled, "resources.jte"), Map.of(), new MockHttpServletRequest()))
                .contains("href=\"/css/bundle.css?theme=dark&amp;lang=en#style\"");
    }

    @Test
    void cachedViewKeepsConcurrentRequestsIsolated() throws Exception {
        JteView view = view(engine, "resources.jte");
        MockHttpServletRequest request = new MockHttpServletRequest();
        ResourceUrlProvider provider = resourceProvider();
        request.setAttribute(ResourceUrlProviderExposingInterceptor.RESOURCE_URL_PROVIDER_ATTR, provider);
        // Compile before concurrent renders so this test exercises request isolation.
        render(view, Map.of(), request);
        var executor = Executors.newFixedThreadPool(4);
        try {
            var tasks = new ArrayList<Callable<String>>();
            for (int i = 0; i < 20; i++) {
                String contextPath = "/app" + i;
                tasks.add(() -> {
                    MockHttpServletRequest current = new MockHttpServletRequest("GET", contextPath + "/page");
                    current.setContextPath(contextPath);
                    current.setAttribute(ResourceUrlProviderExposingInterceptor.RESOURCE_URL_PROVIDER_ATTR, provider);
                    return render(view, Map.of(), current);
                });
            }
            var results = executor.invokeAll(tasks, 30, TimeUnit.SECONDS);
            for (int i = 0; i < results.size(); i++) {
                assertThat(results.get(i).get()).contains("href=\"/app" + i + "/css/bundle.css?")
                        .contains("src=\"/app" + i + "/js/bundle.js\"");
            }
        } finally {
            executor.shutdownNow();
        }
    }

    private static ResourceUrlProvider resourceProvider() throws Exception {
        ResourceHttpRequestHandler handler = new ResourceHttpRequestHandler();
        handler.setLocations(List.of(new ClassPathResource("static/")));
        handler.afterPropertiesSet();
        ResourceUrlProvider provider = new ResourceUrlProvider();
        provider.setHandlerMap(Map.of("/**", handler));
        return provider;
    }

    private static JteView view(TemplateEngine engine, String template) {
        JteView view = new JteView(engine);
        view.setUrl(template);
        return view;
    }

    private static String render(JteView view, Map<String, Object> model, MockHttpServletRequest request) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        view.renderMergedTemplateModel(Collections.unmodifiableMap(model), request, response);
        return response.getContentAsString();
    }
}
