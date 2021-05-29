package gg.jte;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class NativeTest {
    private static final String command = System.getProperty("nativeApp");

    private StringWriter output = new StringWriter();
    private int exit = -1;

    @BeforeEach
    void setup() {
        output = new StringWriter();
        exit = -1;
    }

    @Test
    void helloWorld() throws IOException {
        whenTemplateIsRendered("helloWorld.jte");
        thenOutputIs("Hello World");
    }

    @Test
    void templateNotFound() throws IOException {
        thenRenderingFailsWithError("unknown.jte");
        thenOutputContains("Failed to load unknown.jte");
    }


    @Test
    void exceptionLineNumber1() throws IOException {
        thenRenderingFailsWithError("exceptionLineNumber1.jte");
        thenOutputContains("Caused by: java.lang.NullPointerException");
        thenOutputContains("Failed to render exceptionLineNumber1.jte, error at exceptionLineNumber1.jte:5");
    }

    @Test
    void unusedTag() throws IOException {
        whenTagIsRendered("tag/unused.jte", "param1", "One", "param2", "Two");
        thenOutputIs("One is One, two is Two.");
    }

    private void whenTagIsRendered(String templateName, String key1, String value1, String key2, String value2) throws IOException {
        doExecute("--template", templateName, "--param", key1 + "=" + value1, "--param", key2 + "=" + value2);
    }

    private void whenTemplateIsRendered(String templateName) throws IOException {
        doExecute("--template", templateName);
    }

    private void doExecute(String... args) throws IOException {
        List<String> commandAndArgs = new ArrayList<>();
        commandAndArgs.add(command);
        for (String arg: args) {
            commandAndArgs.add(arg);
        }
        Process p = new ProcessBuilder(commandAndArgs)
                .redirectErrorStream(true) // combine stderr into stdout
                .start();
        Reader r = new InputStreamReader(p.getInputStream());
        for (int c = r.read(); c > -1; c = r.read()) {
            output.write(c);
        }
        try {
            p.waitFor();
        } catch (InterruptedException e) {
            // ignore, not expected
        }
        exit = p.exitValue();
    }

    private void thenRenderingFailsWithError(String templateName) throws IOException {
        whenTemplateIsRendered(templateName);
        assertThat(exit).isGreaterThan(0);
    }

    private void thenOutputIs(String expected) {
        assertThat(output.toString()).isEqualTo(expected);
    }

    private void thenOutputContains(String expected) {
        assertThat(output.toString()).containsOnlyOnce(expected);
    }
}
