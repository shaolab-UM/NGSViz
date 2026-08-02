package com.NGSViz.cli;

/** Exit codes shared by native CLI commands. */
public enum CliExitCode {
    SUCCESS(0),
    USAGE_ERROR(2),
    ENVIRONMENT_ERROR(3),
    INPUT_ERROR(4),
    EXECUTION_ERROR(5),
    OUTPUT_ERROR(6);

    private final int code;

    CliExitCode(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
