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

import gg.jte.TemplateEngine;
import java.util.Map;
import java.util.HashMap;
import gg.jte.models.runtime.*;
@for(String imp: imports)
    import ${imp};
@endfor

${modelConfig.implementationAnnotation()}
${modelConfig.dynamicImplementationAnnotation()}
public class ${targetClassName} implements ${interfaceName} {
    private final TemplateEngine engine;

    public ${targetClassName}(TemplateEngine engine) {
        this.engine = engine;
    }

    @for(TemplateDescription template: templates)
        @template.dynamictemplates.method(template = template)
    @endfor
}