package gg.jte.springframework.boot.autoconfigure;

import gg.jte.CodeResolver;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.DirectoryCodeResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.reactive.result.view.UrlBasedViewResolver;

import java.nio.file.FileSystems;
import java.nio.file.Paths;

@Configuration
@ConditionalOnClass({TemplateEngine.class, UrlBasedViewResolver.class})
@EnableConfigurationProperties(JteProperties.class)
public class ReactiveJteAutoConfiguration {
    private final JteProperties jteProperties;

    public ReactiveJteAutoConfiguration(JteProperties jteProperties) {
        this.jteProperties = jteProperties;
    }

    @Bean
    @ConditionalOnMissingBean(ReactiveJteViewResolver.class)
    public ReactiveJteViewResolver reactiveJteViewResolver(TemplateEngine templateEngine) {

        return new ReactiveJteViewResolver(templateEngine,jteProperties.getTemplateSuffix());
    }

    @Bean
    @ConditionalOnMissingBean(TemplateEngine.class)
    public TemplateEngine jteTemplateEngine() {

        if (jteProperties.usePreCompiledTemplates()) {
            // Templates will need to be compiled by the maven/gradle build task
            return TemplateEngine.createPrecompiled(ContentType.Html);
        } else {
            // Here, a jte file watcher will recompile the jte templates upon file save (the web browser will auto-refresh)
            // If using IntelliJ, use Ctrl-F9 to trigger an auto-refresh when editing non-jte files.
            String[] split = jteProperties.getTemplateLocation().split("/");
            CodeResolver codeResolver = new DirectoryCodeResolver(FileSystems.getDefault().getPath("", split));
            TemplateEngine templateEngine = TemplateEngine.create(codeResolver, Paths.get("jte-classes"), ContentType.Html, getClass().getClassLoader());
            templateEngine.setBinaryStaticContent(true);
            return templateEngine;
        }
    }
}
