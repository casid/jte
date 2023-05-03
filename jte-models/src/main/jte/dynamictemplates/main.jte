@import gg.jte.extension.*
@import java.util.Set

@param String targetClassName
@param String interfaceName
@param JteConfig config
@param Set<TemplateDescription> templates
@param Iterable<String> imports

package ${config.packageName()};

import gg.jte.TemplateEngine;
import java.util.Map;
import java.util.HashMap;
import gg.jte.models.runtime.*;
@for(String imp: imports)
    import ${imp};@endfor

public class ${targetClassName} implements ${interfaceName} {
    private final TemplateEngine engine;

    public DynamicTemplates(TemplateEngine engine) {
        this.engine = engine;
    }

    @for(TemplateDescription template: templates)
        @template.dynamictemplates.method(template = template)
    @endfor
}