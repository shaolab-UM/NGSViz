package com.NGSViz.cli;

import java.io.PrintStream;
import java.util.List;

/** Dispatches native CLI commands without terminating the JVM. */
public final class CliDispatcher {
    private static final List<String> LEGACY_FLAGS = List.of(
            "-H", "-V", "-J", "-G", "-R", "-I", "-O", "-T", "-DB", "-CP",
            "-D", "-X", "-A", "-B", "-F", "-N", "-S", "-BD", "-DP", "-P",
            "-BM", "-BS", "-MQ", "-FL", "-SS", "-NF", "-CM"
    );

    private final PrintStream output;

    public CliDispatcher(PrintStream output) {
        this.output = output;
    }

    public static boolean shouldDispatch(String[] args) {
        return args.length > 0 && !args[0].startsWith("-");
    }

    static List<String> legacyFlags() {
        return LEGACY_FLAGS;
    }

    public int dispatch(String[] args) {
        String commandName = args.length == 0 ? "" : args[0];
        CliCommand command = CliCommand.fromName(commandName);
        CliResponse response;

        if (command == null) {
            response = CliResponse.error(
                    commandName, "Unknown command: " + commandName + ".",
                    "CLI_UNKNOWN_COMMAND"
            );
        } else if (command == CliCommand.DESCRIBE) {
            response = new DescribeCommand().execute(args);
        } else {
            response = dispatchCompute(command, args);
        }

        output.println(response.toJson().toString());
        return response.exitCode();
    }

    private CliResponse dispatchCompute(CliCommand command, String[] args) {
        if (containsRequest(args) && containsLegacyFlag(args)) {
            return CliResponse.error(
                    command.commandName(),
                    "--request cannot be combined with legacy computation flags.",
                    "CLI_INPUT_MODE_CONFLICT"
            );
        }
        if (args.length != 3 || !"--request".equals(args[1])
                || args[2].isEmpty()) {
            return CliResponse.error(
                    command.commandName(),
                    "Compute commands require only --request <json>.",
                    "CLI_ARGUMENT_ERROR"
            );
        }
        return CliResponse.unsupported(command.commandName());
    }

    private boolean containsRequest(String[] args) {
        for (String arg : args) {
            if ("--request".equals(arg)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsLegacyFlag(String[] args) {
        for (int index = 1; index < args.length; index++) {
            if (LEGACY_FLAGS.contains(args[index])) {
                return true;
            }
        }
        return false;
    }
}
