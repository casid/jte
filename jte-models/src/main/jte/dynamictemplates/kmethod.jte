@import gg.jte.extension.api.*
@import gg.jte.models.generator.Util

@param TemplateDescription template

    override fun ${Util.methodName(template)}(${Util.kotlinTypedParams(template, false)}): JteModel {
        val paramMap = mapOf<String, Any?>(
        @for(ParamDescription param: template.params())
            "${param.name()}" to ${param.name()},@endfor
        )
        return DynamicJteModel(engine, "${template.name()}", paramMap);
    }
