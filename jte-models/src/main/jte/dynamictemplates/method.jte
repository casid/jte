@import gg.jte.extension.api.*
@import gg.jte.models.generator.Util

@param TemplateDescription template

    @Override
    public JteModel ${Util.methodName(template)}(${Util.typedParams(template)}) {
        Map<String, Object> paramMap = new HashMap<>();
        @for(ParamDescription param: template.params())
        paramMap.put("${param.name()}", ${param.name()});@endfor
        return new DynamicJteModel(engine, "${template.name()}", paramMap);
    }
