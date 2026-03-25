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
@javax.annotation.processing.Generated("gg.jte.TemplateEngine")
public class ${targetClassName} implements ${interfaceName} {
    @if("true".equalsIgnoreCase(modelConfig.staticImplementationSingleton()))
    public static final ${targetClassName} INSTANCE = new ${targetClassName}();
    public static final class Singleton {
        @for(TemplateDescription template : templates)
        !{final String methodName = Util.methodName(template);}
        public static JteModel ${methodName}(${Util.typedParams(template)}) {
            return INSTANCE.${methodName}(${template.params().stream().map(ParamDescription::name).collect(Collectors.joining(","))});
        }
        @endfor
    }
    @endif
    @for(TemplateDescription template : templates)
        @template.statictemplates.method(config = config, template = template)
    @endfor
}