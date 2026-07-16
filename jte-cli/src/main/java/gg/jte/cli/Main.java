package gg.jte.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

@Command(name = "jte-cli", mixinStandardHelpOptions = true,
        description = "Command line tool for jte template generation, without needing Maven or Gradle.",
        subcommands = {GenerateCommand.class})
public class Main implements Callable<Integer> {

    public static void main(String[] args) {
        System.exit(newCommandLine(new Main()).execute(args));
    }

    static CommandLine newCommandLine(Main main) {
        CommandLine commandLine = new CommandLine(main);
        commandLine.setCaseInsensitiveEnumValuesAllowed(true);
        commandLine.getSubcommands().values().forEach(sub -> sub.setCaseInsensitiveEnumValuesAllowed(true));
        return commandLine;
    }

    @Override
    public Integer call() {
        throw new CommandLine.ParameterException(new CommandLine(this), "Missing required subcommand, try 'jte-cli generate --help'.");
    }
}
