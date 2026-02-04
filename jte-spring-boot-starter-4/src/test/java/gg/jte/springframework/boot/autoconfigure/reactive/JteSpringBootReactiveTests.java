package gg.jte.springframework.boot.autoconfigure.reactive;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {"gg.jte.development-mode=true", "spring.main.web-application-type=reactive"})
@AutoConfigureWebTestClient
public class JteSpringBootReactiveTests {

    @Test
    void contextLoads() {
    }

    @Test
    void greeting(@Autowired WebTestClient client) throws Exception {
        client.get()
                .uri("/greet?subject=World")
                .exchange()
                .expectAll(
                        spec -> spec.expectStatus().isOk(),
                        spec -> spec.expectBody(String.class).value(v -> {
                            assertThat(v).contains("Hello World!");
                        })
                );
    }
}
