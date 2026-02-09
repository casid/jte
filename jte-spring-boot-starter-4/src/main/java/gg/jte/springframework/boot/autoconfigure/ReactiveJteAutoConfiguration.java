package gg.jte.springframework.boot.autoconfigure;

import gg.jte.TemplateEngine;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.result.view.UrlBasedViewResolver;

@AutoConfiguration
@ConditionalOnClass({TemplateEngine.class, UrlBasedViewResolver.class})
@ConditionalOnWebApplication(type = Type.REACTIVE)
@EnableConfigurationProperties(JteProperties.class)
public class ReactiveJteAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ReactiveJteViewResolver.class)
    public ReactiveJteViewResolver reactiveJteViewResolver(TemplateEngine templateEngine, JteProperties jteProperties) {
        return new ReactiveJteViewResolver(templateEngine, jteProperties.getTemplateSuffix());
    }
}
