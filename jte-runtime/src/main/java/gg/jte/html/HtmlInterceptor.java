package gg.jte.html;

import gg.jte.TemplateOutput;

import java.util.Map;

@SuppressWarnings("unused") // Called by template code
public interface HtmlInterceptor {
    void onHtmlTagOpened(String name, Map<String, Object> attributes, TemplateOutput output);
    void onHtmlAttributeStarted(String name, Map<String, Object> attributesBefore, TemplateOutput output);
    void onHtmlTagClosed(String name, TemplateOutput output);
}
