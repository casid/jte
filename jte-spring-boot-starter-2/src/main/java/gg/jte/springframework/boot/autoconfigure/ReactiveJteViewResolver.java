package gg.jte.springframework.boot.autoconfigure;

import gg.jte.TemplateEngine;
import org.springframework.core.Ordered;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.result.view.AbstractUrlBasedView;
import org.springframework.web.reactive.result.view.UrlBasedViewResolver;

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
