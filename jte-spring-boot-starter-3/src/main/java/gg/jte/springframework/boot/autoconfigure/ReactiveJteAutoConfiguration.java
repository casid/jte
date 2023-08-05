package gg.jte.springframework.boot.autoconfigure;

import gg.jte.*;
import gg.jte.resolve.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.*;
import org.springframework.context.annotation.*;
import org.springframework.web.reactive.result.view.*;

import java.nio.file.*;

@AutoConfiguration
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

        return new ReactiveJteViewResolver(templateEngine, jteProperties.getTemplateSuffix());
    }

    @Bean
    @ConditionalOnMissingBean(TemplateEngine.class)
    public TemplateEngine jteTemplateEngine() {

        if (jteProperties.usePreCompiledTemplates()) {
            // Templates will be compiled by the maven build task
            return TemplateEngine.createPrecompiled(ContentType.Html);
        } else {
            // Here, a JTE file watcher will recompile the JTE templates upon file save (the web browser will auto-refresh)
            // If using IntelliJ, use Ctrl-F9 to trigger an auto-refresh when editing non-JTE files.
            String[] split = jteProperties.getTemplateLocation().split("/");
            CodeResolver codeResolver = new DirectoryCodeResolver(FileSystems.getDefault().getPath("", split));
            TemplateEngine templateEngine = TemplateEngine.create(codeResolver, Paths.get("jte-classes"), ContentType.Html, getClass().getClassLoader());
            templateEngine.setBinaryStaticContent(true);
            return templateEngine;
        }
    }
}
