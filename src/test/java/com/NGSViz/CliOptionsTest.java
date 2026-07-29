package com.NGSViz;

import com.NGSViz.configSet.GetInputParameterValue;
import com.NGSViz.configSet.InputParameterAttributes;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CliOptionsTest {
    private static final List<String> EXPECTED_HELP_OPTIONS = List.of(
            "-H", "-V", "-J", "-G", "-R", "-I", "-O", "-T", "-DB", "-CP",
            "-D", "-X", "-A", "-B", "-F", "-N", "-S", "-BD", "-DP", "-P",
            "-BM", "-BS", "-MQ", "-FL", "-SS", "-NF", "-CM"
    );

    @Test
    void helpPrintsEveryCommandOptionAndStopsBeforeAnalysis() throws Exception {
        ProcessResult result = runNgsviz("-H");

        assertEquals(0, result.exitCode);
        for (String option : EXPECTED_HELP_OPTIONS) {
            assertTrue(result.output.contains(option), "Missing help option: " + option);
        }
    }

    @Test
    void versionPrintsCurrentVersionAndStopsBeforeAnalysis() throws Exception {
        ProcessResult result = runNgsviz("-V");

        assertEquals(0, result.exitCode);
        assertEquals("ngsViz 1.3", result.output.trim());
    }

    @Test
    void yamlOptionStoresAiConfigurationPath() {
        InputParameterAttributes.ai_config_path = null;

        GetInputParameterValue.getInputParametersValue(
                new String[]{"-J", "agent/examples/config.yaml"}
        );

        assertEquals("agent/examples/config.yaml", InputParameterAttributes.ai_config_path);
    }

    private ProcessResult runNgsviz(String option) throws IOException, InterruptedException {
        String javaExecutable = Path.of(
                System.getProperty("java.home"), "bin", "java"
        ).toString();
        Process process = new ProcessBuilder(
                javaExecutable,
                "-cp",
                System.getProperty("java.class.path"),
                NGSViz.class.getName(),
                option
        ).redirectErrorStream(true).start();
        String output = new String(
                process.getInputStream().readAllBytes(),
                StandardCharsets.UTF_8
        );
        return new ProcessResult(process.waitFor(), output);
    }

    private static class ProcessResult {
        private final int exitCode;
        private final String output;

        private ProcessResult(int exitCode, String output) {
            this.exitCode = exitCode;
            this.output = output;
        }
    }
}
