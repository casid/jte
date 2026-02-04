package gg.jte.springframework.boot.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.result.view.View;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class JteAutoConfigurationReactiveIntegrationTests {

    private final ReactiveWebApplicationContextRunner contextRunner = new ReactiveWebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(JteAutoConfiguration.class, ServletJteAutoConfiguration.class, ReactiveJteAutoConfiguration.class))
            .withPropertyValues("gg.jte.developmentMode:true");

    @Test
    void resolveView() {
        this.contextRunner.run((context) -> {
            ReactiveJteViewResolver resolver = context.getBean(ReactiveJteViewResolver.class);
            Mono<View> view = resolver.resolveViewName("greeting", Locale.UK);
            MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/path"));
            view.flatMap((v) -> v.render(Map.of("subject", "reactive"), MediaType.TEXT_HTML, exchange)).block(Duration.ofSeconds(30));
            String result = exchange.getResponse().getBodyAsString().block(Duration.ofSeconds(30));
            assertThat(result).contains("Hello reactive!");
            assertThat(exchange.getResponse().getHeaders().getContentType()).isEqualTo(MediaType.TEXT_HTML);
        });
    }
}
