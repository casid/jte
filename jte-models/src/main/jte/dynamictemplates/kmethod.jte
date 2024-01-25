@import gg.jte.extension.api.*
@import gg.jte.models.generator.Util

@param TemplateDescription template

    override fun ${Util.methodName(template)}(${Util.kotlinTypedParams(template, false)}): JteModel {
        @if(template.params().isEmpty())
        val paramMap = emptyMap<String, Any?>()
        @else
        val paramMap = buildMap<String, Any?> {
        @for(ParamDescription param: template.params())
            put("${param.name()}", ${param.name()})@endfor
        }
        @endif
        return DynamicJteModel(engine, "${template.name()}", paramMap)
    }
