package gg.jte.springframework.boot.autoconfigure;

import gg.jte.*;
import gg.jte.resolve.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.*;
import org.springframework.context.annotation.*;
import org.springframework.core.env.*;
import org.springframework.web.servlet.view.*;

import java.nio.file.*;

@AutoConfiguration
@ConditionalOnClass({TemplateEngine.class, AbstractTemplateViewResolver.class})
@EnableConfigurationProperties(JteProperties.class)
public class JteAutoConfiguration {

    @Autowired
    private Environment environment;

    @Autowired
    private JteProperties jteProperties;

    @Bean
    @ConditionalOnMissingBean(JteViewResolver.class)
    public JteViewResolver jteViewResolver(TemplateEngine templateEngine) {

        return new JteViewResolver(templateEngine, jteProperties.getTemplateSuffix());
    }


    @Bean
    @ConditionalOnMissingBean(TemplateEngine.class)
    public TemplateEngine jteTemplateEngine() {
        if(jteProperties.usePreCompiledTemplates()){
            // Templates will need to be compiled by the maven/gradle build task
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
