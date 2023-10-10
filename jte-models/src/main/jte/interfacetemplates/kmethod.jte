@import gg.jte.extension.api.*
@import gg.jte.models.generator.Util

@param TemplateDescription template

    @JteView("${template.name()}")
    fun ${Util.methodName(template)}(${Util.kotlinTypedParams(template, true)}): JteModel
