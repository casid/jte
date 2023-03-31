package gg.jte.generated.precompiled;
@javax.annotation.processing.Generated(value = "gg.jte.compiler.java.JavaCodeGenerator", comments="template name helloWorld.jte")
public final class JtehelloWorldGenerated {
	public static final String JTE_NAME = "helloWorld.jte";
	public static final int[] JTE_LINE_INFO = {0,0,0,0,0,1,1,1,1};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, test.Model model) {
		jteOutput.setContext("html", null);
		jteOutput.writeUserContent(model.hello);
		jteOutput.writeContent(" World");
	}
	public static void renderMap(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		test.Model model = (test.Model)params.get("model");
		render(jteOutput, jteHtmlInterceptor, model);
	}
}
