@import gg.jte.extension.api.*
@import gg.jte.models.generator.ModelConfig
@import java.util.Set

@param String targetClassName
@param JteConfig config
@param Set<TemplateDescription> templates
@param Iterable<String> imports
@param ModelConfig modelConfig

package ${config.packageName()};
import gg.jte.models.runtime.*;
    @for(String imp: imports)
import ${imp};@endfor

${modelConfig.interfaceAnnotation()}
public interface ${targetClassName} {
    @for(TemplateDescription template: templates)
        @template.interfacetemplates.method(template = template)
    @endfor
}