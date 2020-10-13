package gg.jte.html.support;

public final class HtmlSupport {
    public static CssClasses addClass(String cssClass) {
        return new CssClasses().addClass(cssClass);
    }

    public static CssClasses addClassIf(boolean condition, String cssClass) {
        return new CssClasses().addClassIf(condition, cssClass);
    }
}
