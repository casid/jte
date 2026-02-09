package gg.jte.springframework.boot.autoconfigure;

import gg.jte.TemplateEngine;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.view.AbstractTemplateViewResolver;

@AutoConfiguration
@ConditionalOnClass({TemplateEngine.class, AbstractTemplateViewResolver.class})
@ConditionalOnWebApplication(type = Type.SERVLET)
@EnableConfigurationProperties(JteProperties.class)
public class ServletJteAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(JteViewResolver.class)
    public JteViewResolver jteViewResolver(TemplateEngine templateEngine, JteProperties jteProperties) {
        return new JteViewResolver(templateEngine, jteProperties);
    }
}
