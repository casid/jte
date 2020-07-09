package org.jusecase.jte.support;

import org.jusecase.jte.TemplateOutput;

import java.util.Map;

public interface HtmlTagSupport {
    void onHtmlTagOpened(String name, Map<String, String> attributes, TemplateOutput output);
    void onHtmlTagClosed(String name, TemplateOutput output);
}
