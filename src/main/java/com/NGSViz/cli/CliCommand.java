package com.NGSViz.cli;

import org.json.JSONArray;

import java.util.Arrays;

/** Native CLI commands introduced by the machine-readable interface. */
public enum CliCommand {
    DESCRIBE("describe"),
    VALIDATE_COMPUTE("validate-compute"),
    RUN_COMPUTE("run-compute");

    private final String commandName;

    CliCommand(String commandName) {
        this.commandName = commandName;
    }

    public String commandName() {
        return commandName;
    }

    public static CliCommand fromName(String commandName) {
        return Arrays.stream(values())
                .filter(command -> command.commandName.equals(commandName))
                .findFirst()
                .orElse(null);
    }

    public static JSONArray namesAsJson() {
        JSONArray names = new JSONArray();
        for (CliCommand command : values()) {
            names.put(command.commandName);
        }
        return names;
    }
}
