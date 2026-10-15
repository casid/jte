package gg.jte.springframework.boot.autoconfigure.servlet;

import jakarta.servlet.RequestDispatcher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"gg.jte.development-mode=true", "spring.main.web-application-type=servlet"})
@AutoConfigureMockMvc
class JteSpringBootServletErrorViewTests {

    private static MockHttpServletRequestBuilder errorRequest(int status) {
        return get("/error")
                .accept(MediaType.TEXT_HTML)
                .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, status)
                .requestAttr(RequestDispatcher.ERROR_REQUEST_URI, "/missing");
    }

    @Test
    void statusSpecificTemplateIsUsed(@Autowired MockMvc mvc) throws Exception {
        mvc.perform(errorRequest(404))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"))
                .andExpect(content().string(containsString("Custom 404 page")));
    }

    @Test
    void anyStatusCanHaveASpecificTemplate(@Autowired MockMvc mvc) throws Exception {
        mvc.perform(errorRequest(418))
                .andExpect(status().isIAmATeapot())
                .andExpect(view().name("error/418"))
                .andExpect(content().string(containsString("Custom 418 page")));
    }

    @Test
    void clientErrorTemplateIsUsedWithoutSpecificTemplate(@Autowired MockMvc mvc) throws Exception {
        mvc.perform(errorRequest(403))
                .andExpect(status().isForbidden())
                .andExpect(view().name("error/4xx"))
                .andExpect(content().string(containsString("Custom 4xx page")));
    }

    @Test
    void serverErrorTemplateIsUsed(@Autowired MockMvc mvc) throws Exception {
        mvc.perform(errorRequest(503))
                .andExpect(status().isServiceUnavailable())
                .andExpect(view().name("error/5xx"))
                .andExpect(content().string(containsString("Custom 5xx page")));
    }

    @Test
    void statusOutsideErrorSeriesFallsBackToBootDefault(@Autowired MockMvc mvc) throws Exception {
        mvc.perform(errorRequest(302))
                .andExpect(view().name("error"));
    }
}
