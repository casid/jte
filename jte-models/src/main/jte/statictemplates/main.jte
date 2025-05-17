@import gg.jte.extension.api.*
@import gg.jte.models.generator.ModelConfig
@import java.util.Set

@param String targetClassName
@param String interfaceName
@param JteConfig config
@param Set<TemplateDescription> templates
@param Iterable<String> imports
@param ModelConfig modelConfig

package ${config.packageName()};

import gg.jte.models.runtime.*;
import gg.jte.ContentType;
import gg.jte.TemplateOutput;
import gg.jte.html.HtmlInterceptor;
import gg.jte.html.HtmlTemplateOutput;
@for(String imp: imports)
    import ${imp};
@endfor

${modelConfig.implementationAnnotation()}
${modelConfig.staticImplementationAnnotation()}
public class ${targetClassName} implements ${interfaceName} {
    @for(TemplateDescription template: templates)
        @template.statictemplates.method(config = config, template = template)
    @endfor
}