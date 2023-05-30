@import gg.jte.ContentType
@import gg.jte.extension.api.*
@import gg.jte.models.generator.Util

@param JteConfig config
@param TemplateDescription template

!{String outputClass = config.contentType() == ContentType.Html ? "HtmlTemplateOutput" : "TemplateOutput";}
    @Override
    public JteModel ${Util.methodName(template)}(${Util.typedParams(template)}) {
        return new StaticJteModel<${outputClass}>(
            ContentType.${config.contentType()},
            (output, interceptor) -> ${template.fullyQualifiedClassName()}.render(output, interceptor${Util.paramNames(template)}),
            "${template.name()}",
            "${template.packageName()}",
            ${template.fullyQualifiedClassName()}.JTE_LINE_INFO
        );
    }
