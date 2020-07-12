package org.jusecase.jte.support;

import org.jusecase.jte.TemplateOutput;

import java.util.Map;

@SuppressWarnings("unused") // Called by template code
public interface HtmlTagSupport {
    void onHtmlTagOpened(String name, Map<String, Object> attributes, TemplateOutput output);
    void onHtmlTagClosed(String name, TemplateOutput output);
}
