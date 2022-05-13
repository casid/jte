package test;

import gg.jte.*;
import gg.jte.output.StringOutput;
import picocli.CommandLine;
import test.Model;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Need an application with a main method as the entry point for a native image
 *
 * @author eharman
 * @since 2021-03-14
 */
@CommandLine.Command(
        name = "nativeTestApp",
        mixinStandardHelpOptions = true
)
public class NativeTestApp implements Callable<Integer> {
    public static void main(String[] args) {
        // see https://picocli.info/#execute
        System.exit(
                new CommandLine(new NativeTestApp()).execute(args)
        );
    }

    @CommandLine.Option(names = {"--template"}, required = true)
    private String template;

    @CommandLine.Option(names = {"-p", "--param"})
    private Map<String, Object> params;

    @Override
    public Integer call() throws Exception {
        TemplateEngine templateEngine = TemplateEngine.createPrecompiled(ContentType.Html);
        Model model = new Model();
        model.hello = "Hello";
        model.x = 42;

        TemplateOutput output = new StringOutput();
        if (params != null && !params.isEmpty()) {
            templateEngine.render(template, params, output);
        } else {
            templateEngine.render(template, model, output);
        }
        System.out.print(output.toString());
        return 0;
    }

}
