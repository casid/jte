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

import java.nio.file.FileSystems;
import java.nio.file.Paths;

@AutoConfiguration
@ConditionalOnClass(TemplateEngine.class)
@EnableConfigurationProperties(JteProperties.class)
public class JteAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(TemplateEngine.class)
    public TemplateEngine jteTemplateEngine(JteProperties jteProperties) {
        if (jteProperties.isDevelopmentMode() && jteProperties.usePreCompiledTemplates()) {
            throw new JteConfigurationException("You can't use development mode and precompiledTemplates together");
        }
        if (jteProperties.usePreCompiledTemplates()) {
            // Templates will need to be compiled by the maven/gradle build task
            return TemplateEngine.createPrecompiled(ContentType.Html);
        }
        if (jteProperties.isDevelopmentMode()) {
            // Here, a jte file watcher will recompile the jte templates upon file save (the web browser will auto-refresh)
            // If using IntelliJ, use Ctrl-F9 to trigger an auto-refresh when editing non-jte files.
            String[] split = jteProperties.getTemplateLocation().split("/");
            CodeResolver codeResolver = new DirectoryCodeResolver(FileSystems.getDefault().getPath("", split));
            TemplateEngine templateEngine = TemplateEngine.create(codeResolver, Paths.get("jte-classes"), ContentType.Html, getClass().getClassLoader());
            templateEngine.setTrimControlStructures(jteProperties.isTrimControlStructures());
            return templateEngine;
        }
        throw new JteConfigurationException("You need to either set gg.jte.usePrecompiledTemplates or gg.jte.developmentMode to true ");
    }
}
