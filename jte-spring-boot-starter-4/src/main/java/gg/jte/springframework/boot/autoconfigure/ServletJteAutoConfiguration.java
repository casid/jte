package gg.jte.springframework.boot.autoconfigure;

import gg.jte.TemplateEngine;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.webmvc.autoconfigure.error.DefaultErrorViewResolver;
import org.springframework.boot.webmvc.autoconfigure.error.ErrorViewResolver;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.view.AbstractTemplateViewResolver;

@AutoConfiguration(beforeName = "org.springframework.boot.webmvc.autoconfigure.error.ErrorMvcAutoConfiguration")
@ConditionalOnClass({TemplateEngine.class, AbstractTemplateViewResolver.class})
@ConditionalOnWebApplication(type = Type.SERVLET)
@EnableConfigurationProperties(JteProperties.class)
public class ServletJteAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(JteViewResolver.class)
    public JteViewResolver jteViewResolver(TemplateEngine templateEngine, JteProperties jteProperties) {
        return new JteViewResolver(templateEngine, jteProperties);
    }

    /**
     * Registering an {@link ErrorViewResolver} makes Boot back off from its own, so the default resolver
     * is kept as fallback (static error pages, other template engines).
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass({ErrorViewResolver.class, DefaultErrorViewResolver.class})
    @ConditionalOnBooleanProperty(name = "gg.jte.error-view.enabled", matchIfMissing = true)
    @EnableConfigurationProperties(WebProperties.class)
    static class JteErrorViewResolverConfiguration {

        @Bean
        @ConditionalOnMissingBean(ErrorViewResolver.class)
        public JteErrorViewResolver jteErrorViewResolver(TemplateEngine templateEngine, JteProperties jteProperties,
                                                         ApplicationContext applicationContext, WebProperties webProperties) {
            ErrorViewResolver fallback = new DefaultErrorViewResolver(applicationContext, webProperties.getResources());
            return new JteErrorViewResolver(templateEngine, jteProperties, fallback);
        }
    }
}
