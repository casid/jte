package gg.jte.springframework.boot.autoconfigure;

import gg.jte.CodeResolver;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.DirectoryCodeResolver;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.devtools.filewatch.FileSystemWatcher;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextRefreshedEvent;
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


    Logger logger = LoggerFactory.getLogger(JteAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean(JteViewResolver.class)
    public JteViewResolver jteViewResolver(TemplateEngine templateEngine) {
        return new JteViewResolver(templateEngine, jteProperties);
    }

    @Bean
    @ConditionalOnMissingBean(TemplateEngine.class)
    public TemplateEngine jteTemplateEngine() {
        if(jteProperties.isDevelopmentMode() && jteProperties.usePreCompiledTemplates()){
            throw new JteConfigurationException("You can't use development mode and precompiledTemplates together");
        }
        if (jteProperties.usePreCompiledTemplates()) {
            // Templates will need to be compiled by the maven/gradle build task
            return TemplateEngine.createPrecompiled(ContentType.Html);
        }
        if(jteProperties.isDevelopmentMode()) {
            // Here, a jte file watcher will recompile the jte templates upon file save (the web browser will auto-refresh)
            // If using IntelliJ, use Ctrl-F9 to trigger an auto-refresh when editing non-jte files.
            String[] split = jteProperties.getTemplateLocation().split("/");
            CodeResolver codeResolver = new DirectoryCodeResolver(FileSystems.getDefault().getPath("", split));
            return TemplateEngine.create(codeResolver, Paths.get("jte-classes"), ContentType.Html, getClass().getClassLoader());
        }
        throw new JteConfigurationException("You need to either set gg.jte.usePrecompiledTemplates or gg.jte.developmentMode to true ");
    }

    @Bean
    @ConditionalOnProperty(name = "gg.jte.developmentMode", havingValue = "true")
    FileSystemWatcher viewComponentFileSystemWatcher(ApplicationContext applicationContext, JteProperties jteProperties) {
        var fileSystemWatcher = new FileSystemWatcher();
        File jteDir = new File(jteProperties.getTemplateLocation());
        fileSystemWatcher.addSourceDirectory(jteDir);
        logger.info("Watching for template changes at: {}", jteDir.getAbsoluteFile().getPath());
        fileSystemWatcher.addListener(
            changeSet -> {
                logger.debug("Detected change to template: {}", changeSet);
                applicationContext.publishEvent(new ContextRefreshedEvent(applicationContext));
            }
        );
        fileSystemWatcher.start();
        return fileSystemWatcher;
    }
}
