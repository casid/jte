package gg.jte.springframework.boot.autoconfigure;

import gg.jte.CodeResolver;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.DirectoryCodeResolver;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.view.AbstractTemplateViewResolver;

import java.nio.file.FileSystems;
import java.nio.file.Paths;

@AutoConfiguration
@ConditionalOnClass({TemplateEngine.class, AbstractTemplateViewResolver.class})
@EnableConfigurationProperties(JteProperties.class)
public class JteAutoConfiguration {
    private final JteProperties jteProperties;

    public JteAutoConfiguration(JteProperties jteProperties) {
        this.jteProperties = jteProperties;
    }

    @Bean
    @ConditionalOnMissingBean(JteViewResolver.class)
    public JteViewResolver jteViewResolver(TemplateEngine templateEngine) {

        return new JteViewResolver(templateEngine, jteProperties.getTemplateSuffix());
    }


    @Bean
    @ConditionalOnMissingBean(TemplateEngine.class)
    public TemplateEngine jteTemplateEngine() {
        if (jteProperties.usePreCompiledTemplates()) {
            // Templates will need to be compiled by the maven/gradle build task
            return TemplateEngine.createPrecompiled(ContentType.Html);
        } else {
            // Here, a JTE file watcher will recompile the JTE templates upon file save (the web browser will auto-refresh)
            // If using IntelliJ, use Ctrl-F9 to trigger an auto-refresh when editing non-JTE files.
            String[] split = jteProperties.getTemplateLocation().split("/");
            CodeResolver codeResolver = new DirectoryCodeResolver(FileSystems.getDefault().getPath("", split));
            return TemplateEngine.create(codeResolver, Paths.get("jte-classes"), ContentType.Html, getClass().getClassLoader());
        }

    }
}
