package com.NGSViz.cli;

import com.NGSViz.CliOptions;
import org.json.JSONArray;
import org.json.JSONObject;

/** Implements the native CLI contract description command. */
public final class DescribeCommand {
    public CliResponse execute(String[] args) {
        if (args.length != 3 || !"--format".equals(args[1])
                || !"json".equals(args[2])) {
            return CliResponse.error(
                    CliCommand.DESCRIBE.commandName(),
                    "describe requires --format json.",
                    "CLI_ARGUMENT_ERROR"
            );
        }

        JSONObject data = new JSONObject();
        data.put("version", CliOptions.VERSION);
        data.put("commands", CliCommand.namesAsJson());
        data.put("legacy_flags", new JSONArray(CliDispatcher.legacyFlags()));
        data.put("exit_codes", exitCodes());
        return CliResponse.success(
                CliCommand.DESCRIBE.commandName(), "CLI contract described.", data
        );
    }

    private JSONObject exitCodes() {
        JSONObject exitCodes = new JSONObject();
        exitCodes.put("success", CliExitCode.SUCCESS.code());
        exitCodes.put("usage_error", CliExitCode.USAGE_ERROR.code());
        exitCodes.put("environment_error", CliExitCode.ENVIRONMENT_ERROR.code());
        exitCodes.put("input_error", CliExitCode.INPUT_ERROR.code());
        exitCodes.put("execution_error", CliExitCode.EXECUTION_ERROR.code());
        exitCodes.put("output_error", CliExitCode.OUTPUT_ERROR.code());
        return exitCodes;
    }
}
