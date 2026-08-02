package com.NGSViz.cli;

import org.json.JSONArray;
import org.json.JSONObject;

/** Machine-readable response envelope for native CLI commands. */
public final class CliResponse {
    private static final String SCHEMA_VERSION = "1.0";

    private final String command;
    private final String status;
    private final int exitCode;
    private final String message;
    private final Object data;
    private final JSONArray warnings;
    private final JSONArray errors;

    private CliResponse(String command, String status, int exitCode, String message,
                        Object data, JSONArray warnings, JSONArray errors) {
        this.command = command;
        this.status = status;
        this.exitCode = exitCode;
        this.message = message;
        this.data = data;
        this.warnings = warnings;
        this.errors = errors;
    }

    public static CliResponse success(String command, String message, JSONObject data) {
        return new CliResponse(
                command, "success", CliExitCode.SUCCESS.code(), message, data,
                new JSONArray(), new JSONArray()
        );
    }

    public static CliResponse error(String command, String message, String errorCode) {
        return issueResponse(command, "error", message, errorCode);
    }

    public static CliResponse unsupported(String command) {
        return issueResponse(
                command, "unsupported",
                "Command is not implemented in Phase 2.",
                "COMMAND_UNSUPPORTED"
        );
    }

    private static CliResponse issueResponse(String command, String status,
                                             String message, String errorCode) {
        JSONObject error = new JSONObject();
        error.put("code", errorCode);
        error.put("field", JSONObject.NULL);
        error.put("message", message);
        return new CliResponse(
                command, status, CliExitCode.USAGE_ERROR.code(), message,
                JSONObject.NULL, new JSONArray(), new JSONArray().put(error)
        );
    }

    public int exitCode() {
        return exitCode;
    }

    public JSONObject toJson() {
        JSONObject response = new JSONObject();
        response.put("schema_version", SCHEMA_VERSION);
        response.put("command", command);
        response.put("status", status);
        response.put("exit_code", exitCode);
        response.put("message", message);
        response.put("data", data);
        response.put("warnings", warnings);
        response.put("errors", errors);
        return response;
    }
}
