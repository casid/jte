package gg.jte.springframework.boot.autoconfigure;

import gg.jte.*;
import org.springframework.core.*;
import org.springframework.lang.*;
import org.springframework.web.reactive.result.view.*;

public class ReactiveJteViewResolver extends UrlBasedViewResolver {

    private final TemplateEngine templateEngine;

    public ReactiveJteViewResolver(TemplateEngine templateEngine, String templateSuffix) {
        this.templateEngine = templateEngine;
        this.setSuffix(templateSuffix);
        this.setViewClass(ReactiveJteView.class);
        this.setOrder(Ordered.HIGHEST_PRECEDENCE);
    }

    @Override
    @NonNull
    protected Class<?> requiredViewClass() {
        return ReactiveJteView.class;
    }

    @Override
    @NonNull
    protected AbstractUrlBasedView instantiateView() {
        return new ReactiveJteView(templateEngine);
    }
}
