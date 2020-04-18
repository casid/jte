package org.jusecase.jte.internal;

import org.jusecase.jte.TemplateOutput;

final class EmptyTemplate implements Template<Object> {
    public static final EmptyTemplate INSTANCE = new EmptyTemplate();

    @Override
    public void render(Object o, TemplateOutput output) {
    }
}
