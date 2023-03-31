package gg.jte.generated.precompiled.tag;
@javax.annotation.processing.Generated(value = "gg.jte.compiler.java.JavaCodeGenerator", comments="template name tag/unused.jte")
public final class JteunusedGenerated {
	public static final String JTE_NAME = "tag/unused.jte";
	public static final int[] JTE_LINE_INFO = {0,0,0,0,0,2,2,2,2,2,2,2,2};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, String param1, String param2) {
		jteOutput.writeContent("One is ");
		jteOutput.setContext("html", null);
		jteOutput.writeUserContent(param1);
		jteOutput.writeContent(", two is ");
		jteOutput.setContext("html", null);
		jteOutput.writeUserContent(param2);
		jteOutput.writeContent(".");
	}
	public static void renderMap(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		String param1 = (String)params.get("param1");
		String param2 = (String)params.get("param2");
		render(jteOutput, jteHtmlInterceptor, param1, param2);
	}
}
