package gg.jte.html.support;

public final class HtmlSupport {

    private HtmlSupport() {
    }

    public static CssClasses addClass(String cssClass) {
        return new CssClasses().addClass(cssClass);
    }

    public static CssClasses addClass(boolean condition, String cssClass) {
        return new CssClasses().addClass(condition, cssClass);
    }

    public static CssClasses addClass(boolean condition, String cssClass, String otherCssClass) {
        return new CssClasses().addClass(condition, cssClass, otherCssClass);
    }
}
