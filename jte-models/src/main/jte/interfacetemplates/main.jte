@import gg.jte.extension.*
@import java.util.Set

@param String targetClassName
@param JteConfig config
@param Set<TemplateDescription> templates
@param Iterable<String> imports

package ${config.packageName()};
import gg.jte.models.runtime.*;
    @for(String imp: imports)
import ${imp};@endfor

public interface ${targetClassName} {
    @for(TemplateDescription template: templates)
        @template.interfacetemplates.method(template = template)
    @endfor
}