package gg.jte.generated.precompiled;
import test.Model;
public final class JteexceptionLineNumber1Generated {
	public static final String JTE_NAME = "exceptionLineNumber1.jte";
	public static final int[] JTE_LINE_INFO = {0,0,2,2,2,4,4,4,4};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, Model model) {
		jteOutput.writeContent("\n");
		jteOutput.setContext("html", null);
		jteOutput.writeUserContent(model.getThatThrows());
	}
	public static void renderMap(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		Model model = (Model)params.get("model");
		render(jteOutput, jteHtmlInterceptor, model);
	}
}
