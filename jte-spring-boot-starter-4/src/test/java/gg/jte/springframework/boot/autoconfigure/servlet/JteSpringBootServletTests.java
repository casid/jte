package gg.jte.springframework.boot.autoconfigure.servlet;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"gg.jte.development-mode=true", "spring.main.web-application-type=servlet"})
@AutoConfigureMockMvc
public class JteSpringBootServletTests {

    @Test
    void contextLoads() {
    }

    @Test
    void greeting(@Autowired MockMvc mvc) throws Exception {
        mvc.perform(get("/greet?subject=World"))
                .andExpect(status().isOk())
                .andExpect(view().name("greeting"))
                .andExpect(model().attribute("subject", "World"))
                .andExpect(content().string(containsString("Hello World!")));
    }
}
