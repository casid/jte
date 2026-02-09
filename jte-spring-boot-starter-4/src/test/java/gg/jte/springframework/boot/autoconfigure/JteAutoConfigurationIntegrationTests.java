package gg.jte.springframework.boot.autoconfigure;

import jakarta.servlet.ServletContext;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.support.RequestContext;

import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class JteAutoConfigurationIntegrationTests {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(JteAutoConfiguration.class, ServletJteAutoConfiguration.class, ReactiveJteAutoConfiguration.class))
            .withPropertyValues("gg.jte.developmentMode:true");

    @Test
    void resolveView() {
        this.contextRunner.run((context) -> {
            View view = context.getBean(JteViewResolver.class)
                    .resolveViewName("greeting", Locale.UK);
            assertThat(view).isNotNull();
            MockHttpServletResponse response = new MockHttpServletResponse();
            MockHttpServletRequest request = new MockHttpServletRequest(context.getBean(ServletContext.class));
            request.setAttribute(RequestContext.WEB_APPLICATION_CONTEXT_ATTRIBUTE, context);
            view.render(Map.of("subject", "servlet"), request, response);
            String result = response.getContentAsString();
            assertThat(result).contains("Hello servlet!");
            context.close();
        });
    }
}
