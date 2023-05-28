@import gg.jte.extension.*
@import gg.jte.models.generator.Util

@param TemplateDescription template

    @JteView("${template.name()}")
    JteModel ${Util.methodName(template)}(${Util.typedParams(template)});
