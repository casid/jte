@import gg.jte.extension.api.*
@import gg.jte.models.generator.ModelConfig
@import java.util.Set

@param String targetClassName
@param String interfaceName
@param JteConfig config
@param Set<TemplateDescription> templates
@param Iterable<String> imports
@param ModelConfig modelConfig

@file:Suppress("ktlint")
package ${config.packageName()}

import gg.jte.models.runtime.*
import gg.jte.ContentType
import gg.jte.html.HtmlTemplateOutput
@for(String imp: imports)
import ${imp}
@endfor

${modelConfig.implementationAnnotation()}
class ${targetClassName} : ${interfaceName} {
    @for(TemplateDescription template: templates)
        @template.statictemplates.kmethod(config = config, template = template)
    @endfor
}