package com.NGSViz.cli;

import com.NGSViz.NGSViz;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CliDispatcherTest {
    @Test
    void describeReturnsMachineReadableContract() {
        DispatchResult result = dispatch("describe", "--format", "json");

        assertEquals(0, result.exitCode);
        JSONObject response = new JSONObject(result.output);
        assertEquals("describe", response.getString("command"));
        assertEquals("success", response.getString("status"));
        assertEquals("1.3", response.getJSONObject("data").getString("version"));
        assertEquals(
                new JSONArray("[\"describe\",\"validate-compute\",\"run-compute\"]").toList(),
                response.getJSONObject("data").getJSONArray("commands").toList()
        );
        assertTrue(response.getJSONObject("data").getJSONArray("legacy_flags")
                .toList().contains("-G"));
        assertEquals(2, response.getJSONObject("data")
                .getJSONObject("exit_codes").getInt("usage_error"));
    }

    @Test
    void unknownSubcommandReturnsUsageError() {
        DispatchResult result = dispatch("unknown-command");

        assertEquals(2, result.exitCode);
        JSONObject response = new JSONObject(result.output);
        assertEquals("unknown-command", response.getString("command"));
        assertEquals("error", response.getString("status"));
        assertEquals("CLI_UNKNOWN_COMMAND",
                response.getJSONArray("errors").getJSONObject(0).getString("code"));
    }

    @Test
    void describeWithoutFormatValueReturnsUsageError() {
        DispatchResult result = dispatch("describe", "--format");

        assertEquals(2, result.exitCode);
        JSONObject response = new JSONObject(result.output);
        assertEquals("CLI_ARGUMENT_ERROR",
                response.getJSONArray("errors").getJSONObject(0).getString("code"));
    }

    @Test
    void leadingDashKeepsLegacyArgumentsOutOfNativeDispatcher() {
        assertFalse(CliDispatcher.shouldDispatch(new String[]{"-G", "hg38"}));
        assertFalse(CliDispatcher.shouldDispatch(new String[]{"-J", "config.yaml"}));
        assertTrue(CliDispatcher.shouldDispatch(new String[]{"describe", "--format", "json"}));
        assertTrue(CliDispatcher.shouldDispatch(new String[]{"unknown-command"}));
    }

    @Test
    void firstArgumentRoutesNativeCommandBeforeLegacyInformationFlags() throws Exception {
        DispatchResult result = runNgsviz(
                "describe", "--format", "json", "-H"
        );

        assertEquals(2, result.exitCode);
        JSONObject response = new JSONObject(result.output);
        assertEquals("CLI_ARGUMENT_ERROR",
                response.getJSONArray("errors").getJSONObject(0).getString("code"));
    }

    @Test
    void requestMixedWithLegacyFlagReturnsUsageError() {
        DispatchResult result = dispatch(
                "run-compute", "--request", "request.json", "-G", "hg38"
        );

        assertEquals(2, result.exitCode);
        JSONObject response = new JSONObject(result.output);
        assertEquals("CLI_INPUT_MODE_CONFLICT",
                response.getJSONArray("errors").getJSONObject(0).getString("code"));
    }

    @Test
    void computeCommandRejectsLegacyYamlFlag() {
        DispatchResult result = dispatch(
                "validate-compute", "-J", "config.yaml"
        );

        assertEquals(2, result.exitCode);
        JSONObject response = new JSONObject(result.output);
        assertEquals("CLI_ARGUMENT_ERROR",
                response.getJSONArray("errors").getJSONObject(0).getString("code"));
    }

    @Test
    void computeCommandsRemainUnsupportedInPhaseTwo() {
        DispatchResult validate = dispatch(
                "validate-compute", "--request", "request.json"
        );
        DispatchResult run = dispatch(
                "run-compute", "--request", "request.json"
        );

        assertEquals(2, validate.exitCode);
        assertEquals("unsupported", new JSONObject(validate.output).getString("status"));
        assertEquals(2, run.exitCode);
        assertEquals("unsupported", new JSONObject(run.output).getString("status"));
    }

    private DispatchResult dispatch(String... args) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        CliDispatcher dispatcher = new CliDispatcher(
                new PrintStream(output, true, StandardCharsets.UTF_8)
        );
        int exitCode = dispatcher.dispatch(args);
        return new DispatchResult(exitCode, output.toString(StandardCharsets.UTF_8).trim());
    }

    private DispatchResult runNgsviz(String... args) throws IOException, InterruptedException {
        String javaExecutable = Path.of(
                System.getProperty("java.home"), "bin", "java"
        ).toString();
        List<String> command = new ArrayList<>(List.of(
                javaExecutable, "-cp", System.getProperty("java.class.path"),
                NGSViz.class.getName()
        ));
        command.addAll(List.of(args));
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        String output = new String(
                process.getInputStream().readAllBytes(), StandardCharsets.UTF_8
        );
        return new DispatchResult(process.waitFor(), output.trim());
    }

    private static final class DispatchResult {
        private final int exitCode;
        private final String output;

        private DispatchResult(int exitCode, String output) {
            this.exitCode = exitCode;
            this.output = output;
        }
    }
}
