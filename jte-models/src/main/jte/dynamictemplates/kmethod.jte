@import gg.jte.extension.api.*
@import gg.jte.models.generator.Util

@param TemplateDescription template

    override fun ${Util.methodName(template)}(${Util.kotlinTypedParams(template)}): JteModel {
        val paramMap = mapOf(
        @for(ParamDescription param: template.params())
            "${param.name()}" to ${param.name()},@endfor
        )
        return DynamicJteModel(engine, "${template.name()}", paramMap);
    }
