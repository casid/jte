@import gg.jte.extension.api.*
@import gg.jte.models.generator.Util
@import gg.jte.models.generator.ModelConfig
@import java.util.Set
@import java.util.stream.Collectors

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
import gg.jte.TemplateOutput
import gg.jte.html.HtmlTemplateOutput
@for(String imp: imports)
import ${imp}
@endfor

${modelConfig.implementationAnnotation()}
${modelConfig.staticImplementationAnnotation()}
class ${targetClassName} : ${interfaceName} {
    @if("true".equalsIgnoreCase(modelConfig.staticImplementationSingleton()))
    companion object {
        val INSTANCE = ${targetClassName}();
        @for(TemplateDescription template : templates)
        !{final String methodName = Util.methodName(template);}
        fun ${methodName}(${Util.kotlinTypedParams(template, true)}) = INSTANCE.${methodName}(${template.params().stream().map(ParamDescription::name).collect(Collectors.joining(","))});
        @endfor
    }
    @endif
    @for(TemplateDescription template : templates)
        @template.statictemplates.kmethod(config = config, template = template)
    @endfor
}