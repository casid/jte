package gg.jte.springframework;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.DirectoryCodeResolver;
import gg.jte.runtime.Constants;
import jakarta.servlet.http.MappingMatch;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletMapping;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.Model;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ResourceUrlsIntegrationTests.Application.class, properties = {
        "gg.jte.development-mode=true", "gg.jte.template-location=src/test/jte",
        "spring.main.web-application-type=servlet"})
@AutoConfigureMockMvc
class ResourceUrlsIntegrationTests {

    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration
    @Import(ResourceController.class)
    static class Application {
    }

    @Controller
    static class ResourceController {
        @GetMapping("/resources")
        String resources(@RequestParam(name = "resourcePath", required = false) String resourcePath, Model model) {
            if (resourcePath != null) {
                model.addAttribute("resourcePath", resourcePath);
            }
            return "resources";
        }
    }

    @Nested
    @SpringBootTest(classes = Application.class, properties = {
            "gg.jte.development-mode=true", "gg.jte.template-location=src/test/jte", "spring.main.web-application-type=servlet",
            "spring.web.resources.chain.strategy.content.enabled=true",
            "spring.web.resources.chain.strategy.content.paths=/**"})
    class ContentVersioning {

        @Autowired
        MockMvc mvc;

        @Test
        void rendersVersionedResourcesThroughLayoutAndServesOriginalBytes() throws Exception {
            String css = contentUrl("css/bundle.css");
            String js = contentUrl("js/bundle.js");

            String html = mvc.perform(get("/resources"))
                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

            assertThat(html).contains("href=\"" + css + "?theme=dark&amp;lang=en#style\"")
                    .contains("src=\"" + js + "\"");
            mvc.perform(get(css)).andExpect(status().isOk()).andExpect(content().bytes(bytes("css/bundle.css")));
            mvc.perform(get(js)).andExpect(status().isOk()).andExpect(content().bytes(bytes("js/bundle.js")));
        }

        @Test
        void resolvesContextPathWithoutDoublePrefix() throws Exception {
            String css = contentUrl("css/bundle.css");
            String html = mvc.perform(get("/app/resources").contextPath("/app")
                            .param("resourcePath", "/app/css/bundle.css?x=1&y=2#style"))
                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

            assertThat(html).contains("href=\"/app" + css + "?x=1&amp;y=2#style\"");
            mvc.perform(get("/app" + css).contextPath("/app"))
                    .andExpect(status().isOk()).andExpect(content().bytes(bytes("css/bundle.css")));
        }

        @Test
        void resolvesAndServesResourcesWithServletPrefix() throws Exception {
            MockHttpServletMapping mapping = new MockHttpServletMapping("resources", "/mvc/*", "dispatcherServlet", MappingMatch.PATH);
            String html = mvc.perform(get("/app/mvc/resources").contextPath("/app").servletPath("/mvc").pathInfo("/resources")
                            .with(request -> { request.setHttpServletMapping(mapping); return request; })
                            .param("resourcePath", "/app/mvc/css/bundle.css"))
                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

            String css = contentUrl("css/bundle.css");
            assertThat(html).contains("href=\"/app/mvc" + css + "\"");
            mvc.perform(get("/app/mvc" + css).contextPath("/app").servletPath("/mvc").pathInfo(css)
                            .with(request -> { request.setHttpServletMapping(mapping); return request; }))
                    .andExpect(status().isOk()).andExpect(content().bytes(bytes("css/bundle.css")));
        }
    }

    @Nested
    @SpringBootTest(classes = Application.class, properties = {
            "gg.jte.development-mode=true", "gg.jte.template-location=src/test/jte", "spring.main.web-application-type=servlet",
            "spring.web.resources.chain.strategy.fixed.enabled=true",
            "spring.web.resources.chain.strategy.fixed.version=v42", "spring.web.resources.chain.strategy.fixed.paths=/**"})
    class FixedVersioning {

        @Test
        void rendersFixedVersionAndServesOriginalBytes(@Autowired MockMvc mvc) throws Exception {
            String html = mvc.perform(get("/resources"))
                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

            assertThat(html).contains("href=\"/v42/css/bundle.css?theme=dark&amp;lang=en#style\"")
                    .contains("src=\"/v42/js/bundle.js\"");
            mvc.perform(get("/v42/css/bundle.css")).andExpect(status().isOk())
                    .andExpect(content().bytes(bytes("css/bundle.css")));
        }
    }

    @Nested
    class Unversioned {
        @Test
        void rendersOrdinaryResourceUrls(@Autowired MockMvc mvc) throws Exception {
            String html = mvc.perform(get("/resources"))
                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

            assertThat(html).contains("href=\"/css/bundle.css?theme=dark&amp;lang=en#style\"")
                    .contains("src=\"/js/bundle.js\"");
        }
    }

    @Nested
    @SpringBootTest(classes = Application.class, properties = {
            "gg.jte.development-mode=true", "gg.jte.template-location=src/test/jte", "spring.main.web-application-type=servlet",
            "spring.web.resources.add-mappings=false"})
    class NoResourceMappings {
        @Test
        void fallsBackWithoutResourceMappings(@Autowired MockMvc mvc) throws Exception {
            mvc.perform(get("/resources")).andExpect(status().isOk())
                    .andExpect(content().string(containsString("href=\"/css/bundle.css?theme=dark&amp;lang=en#style\"")));
        }
    }

    @Nested
    @SpringBootTest(classes = PrecompiledApplication.class, properties = {
            "gg.jte.development-mode=false", "gg.jte.use-precompiled-templates=true",
            "spring.web.resources.chain.strategy.content.enabled=true",
            "spring.web.resources.chain.strategy.content.paths=/**"})
    class Precompiled {
        @Test
        void rendersVersionedUrlsWithPrecompiledEngine(@Autowired MockMvc mvc) throws Exception {
            mvc.perform(get("/resources")).andExpect(status().isOk())
                    .andExpect(content().string(containsString("href=\"" + contentUrl("css/bundle.css"))));
        }
    }

    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration
    @Import(ResourceController.class)
    static class PrecompiledApplication {
        @Bean
        TemplateEngine templateEngine() {
            Path classes = Path.of("target/resource-url-precompiled");
            TemplateEngine compiler = TemplateEngine.create(new DirectoryCodeResolver(Path.of("src/test/jte")),
                    classes, ContentType.Html, null, Constants.PACKAGE_NAME_PRECOMPILED);
            compiler.precompileAll();
            return TemplateEngine.createPrecompiled(classes, ContentType.Html);
        }
    }

    private static byte[] bytes(String path) throws Exception {
        return new ClassPathResource("static/" + path).getContentAsByteArray();
    }

    private static String contentUrl(String path) throws Exception {
        int dot = path.lastIndexOf('.');
        return "/" + path.substring(0, dot) + "-" + DigestUtils.md5DigestAsHex(bytes(path)) + path.substring(dot);
    }
}
