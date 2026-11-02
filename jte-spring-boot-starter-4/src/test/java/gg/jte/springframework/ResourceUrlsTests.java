package gg.jte.springframework;

import jakarta.servlet.http.MappingMatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mock.web.MockHttpServletMapping;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.util.DigestUtils;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;
import org.springframework.web.servlet.resource.ResourceUrlProvider;
import org.springframework.web.servlet.resource.ResourceUrlProviderExposingInterceptor;
import org.springframework.web.servlet.resource.VersionResourceResolver;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceUrlsTests {

    private static final String CSS = "body { color: blue; }";

    @TempDir
    Path directory;

    @BeforeEach
    void createResources() throws Exception {
        Files.createDirectories(directory.resolve("css"));
        Files.writeString(directory.resolve("css/bundle.css"), CSS);
    }

    @Test
    void resolvesContentVersionAndPreservesQueryAndFragment() throws Exception {
        ResourceUrls resources = resources(contentProvider(), "", "");

        assertThat(resources.url("/css/bundle.css?theme=dark&lang=en#style"))
                .isEqualTo(versioned(CSS) + "?theme=dark&lang=en#style");
        assertThat(resources.url("/css/bundle.css#style?question"))
                .isEqualTo(versioned(CSS) + "#style?question");
    }

    @Test
    void newResourceChainReflectsChangedContent() throws Exception {
        String before = resources(contentProvider(), "", "").url("/css/bundle.css");
        String changed = "body { color: red; }";
        Files.writeString(directory.resolve("css/bundle.css"), changed);

        String after = resources(contentProvider(), "", "").url("/css/bundle.css");

        assertThat(before).isEqualTo(versioned(CSS));
        assertThat(after).isEqualTo(versioned(changed)).isNotEqualTo(before);
    }

    @Test
    void resolvesFixedVersion() throws Exception {
        ResourceUrlProvider provider = provider(new VersionResourceResolver().addFixedVersionStrategy("v42", "/**"));

        assertThat(resources(provider, "", "").url("/css/bundle.css"))
                .isEqualTo("/v42/css/bundle.css");
    }

    @Test
    void preservesOrdinaryPathWithoutVersioning() throws Exception {
        assertThat(resources(provider(new VersionResourceResolver()), "", "").url("/css/bundle.css"))
                .isEqualTo("/css/bundle.css");
        assertThat(resources(provider(new VersionResourceResolver()), "/app", "/mvc").url("/css/bundle.css"))
                .isEqualTo("/app/mvc/css/bundle.css");
    }

    @Test
    void preservesPathOutsideVersionStrategy() throws Exception {
        ResourceUrlProvider provider = provider(new VersionResourceResolver().addContentVersionStrategy("/js/**"));

        assertThat(resources(provider, "", "").url("/css/bundle.css"))
                .isEqualTo("/css/bundle.css");
    }

    @Test
    void addsContextPathOnce() throws Exception {
        ResourceUrls resources = resources(contentProvider(), "/app", "");

        assertThat(resources.url("/css/bundle.css")).isEqualTo("/app" + versioned(CSS));
        assertThat(resources.url("/app/css/bundle.css")).isEqualTo("/app" + versioned(CSS));
        assertThat(resources.url("/application/css/bundle.css")).isEqualTo("/application/css/bundle.css");
    }

    @Test
    void addsContextAndServletPrefixOnce() throws Exception {
        ResourceUrls resources = resources(contentProvider(), "/app", "/mvc");

        assertThat(resources.url("/css/bundle.css?x=1#style"))
                .isEqualTo("/app/mvc" + versioned(CSS) + "?x=1#style");
        assertThat(resources.url("/app/mvc/css/bundle.css"))
                .isEqualTo("/app/mvc" + versioned(CSS));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "css/bundle.css", "../css/bundle.css", "https://example.com/css/bundle.css",
            "http://example.com/css/bundle.css", "//example.com/css/bundle.css", "data:text/css,body{}",
            "#style", "?theme=dark", "mailto:user@example.com", "/css/missing.css?x=1#style", "/unmapped/bundle.css"})
    void leavesUnresolvedAndNonResourceUrlsUnchanged(String url) throws Exception {
        assertThat(resources(contentProvider(), "/app", "").url(url)).isEqualTo(url);
    }

    @Test
    void fallsBackWithoutProviderOrResourceMappings() {
        assertThat(new ResourceUrls(new MockHttpServletRequest()).url("/css/bundle.css?x=1#style"))
                .isEqualTo("/css/bundle.css?x=1#style");
        assertThat(resources(new ResourceUrlProvider(), "/app", "").url("/css/bundle.css"))
                .isEqualTo("/css/bundle.css");
    }

    private ResourceUrlProvider contentProvider() throws Exception {
        return provider(new VersionResourceResolver().addContentVersionStrategy("/**"));
    }

    private ResourceUrlProvider provider(VersionResourceResolver resolver) throws Exception {
        ResourceHttpRequestHandler handler = new ResourceHttpRequestHandler();
        handler.setLocations(List.of(new FileSystemResource(directory.toString() + "/")));
        handler.setResourceResolvers(List.of(resolver, new PathResourceResolver()));
        handler.afterPropertiesSet();
        ResourceUrlProvider provider = new ResourceUrlProvider();
        provider.setHandlerMap(Map.of("/**", handler));
        return provider;
    }

    private static ResourceUrls resources(ResourceUrlProvider provider, String contextPath, String servletPrefix) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", contextPath + servletPrefix + "/page");
        request.setContextPath(contextPath);
        request.setServletPath(servletPrefix.isEmpty() ? "/page" : servletPrefix);
        request.setHttpServletMapping(new MockHttpServletMapping("", servletPrefix.isEmpty() ? "/" : servletPrefix + "/*",
                "dispatcherServlet", servletPrefix.isEmpty() ? MappingMatch.DEFAULT : MappingMatch.PATH));
        request.setAttribute(ResourceUrlProviderExposingInterceptor.RESOURCE_URL_PROVIDER_ATTR, provider);
        return new ResourceUrls(request);
    }

    private static String versioned(String content) {
        return "/css/bundle-" + DigestUtils.md5DigestAsHex(content.getBytes(StandardCharsets.UTF_8)) + ".css";
    }
}
