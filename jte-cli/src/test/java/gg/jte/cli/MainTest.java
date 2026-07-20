package gg.jte.cli;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;

class MainTest {

    @Test
    void printsFriendlyErrorWhenNoSubcommandGiven() {
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(err));
        int exitCode;
        try {
            exitCode = Main.newCommandLine(new Main()).execute();
        } finally {
            System.setErr(originalErr);
        }

        assertThat(exitCode).isNotEqualTo(0);
        assertThat(err.toString()).contains("generate");
    }

    @Test
    void dispatchesToGenerateSubcommand() {
        int exitCode = Main.newCommandLine(new Main()).execute("generate", "--help");

        assertThat(exitCode).isEqualTo(0);
    }
}
