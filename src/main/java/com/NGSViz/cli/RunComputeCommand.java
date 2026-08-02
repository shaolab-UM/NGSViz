package com.NGSViz.cli;

import com.NGSViz.compute.ComputeRequest;
import com.NGSViz.compute.ComputeRequestFormatException;
import com.NGSViz.compute.ComputeRequestReader;
import com.NGSViz.compute.ComputeValidationResult;
import com.NGSViz.compute.ComputeValidator;
import com.NGSViz.compute.ValidationIssue;
import com.NGSViz.configSet.InputParameterAttributes;
import com.NGSViz.coverageCalculator.MainCalculator;
import com.NGSViz.sqldbOperate.exonMode.ExonModelData;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Validates and executes one native Java computation. */
public final class RunComputeCommand {
    private final RequestValidator validator;
    private final RequestMapper mapper;
    private final ComputeExecutor executor;
    private final Clock clock;

    public RunComputeCommand() {
        ComputeParameterMapper parameterMapper = new ComputeParameterMapper();
        this.validator = request -> new ComputeValidator().validate(request);
        this.mapper = parameterMapper::map;
        this.executor = RunComputeCommand::invokeMainCalculator;
        this.clock = Clock.systemUTC();
    }

    RunComputeCommand(RequestValidator validator, RequestMapper mapper,
                      ComputeExecutor executor, Clock clock) {
        this.validator = validator;
        this.mapper = mapper;
        this.executor = executor;
        this.clock = clock;
    }

    CliResponse execute(String requestArgument) {
        Path requestFile = Paths.get(requestArgument).toAbsolutePath().normalize();
        JSONObject data = new JSONObject().put("request_file", requestFile.toString());
        try {
            return execute(new ComputeRequestReader().read(requestFile));
        } catch (ComputeRequestFormatException error) {
            return CliResponse.requestError(
                    CliCommand.RUN_COMPUTE.commandName(),
                    "Compute request format is invalid.", data, error.issues()
            );
        }
    }

    public CliResponse execute(ComputeRequest request) {
        ComputeValidationResult validation = validator.validate(request);
        if (!validation.isValid()) {
            return CliResponse.validation(
                    CliCommand.RUN_COMPUTE.commandName(), validation.context(), validation
            );
        }
        return executeValidated(request, validation);
    }

    private CliResponse executeValidated(ComputeRequest request,
                                         ComputeValidationResult validation) {
        Path output = outputDirectory(request);
        Path log = output.resolve("NGSViz_compute.log");
        Instant started = clock.instant();
        List<ValidationIssue> warnings = new ArrayList<>(validation.warnings());
        List<ValidationIssue> errors = new ArrayList<>();
        ComputeOutputs products = ComputeOutputs.empty();
        int exitCode = CliExitCode.SUCCESS.code();
        try {
            Files.createDirectories(output);
            products = runWithLog(request, validation.context(), output, log);
        } catch (ComputeProductInspector.ProductValidationException error) {
            exitCode = CliExitCode.OUTPUT_ERROR.code();
            errors.add(error.issue());
        } catch (Exception error) {
            exitCode = CliExitCode.EXECUTION_ERROR.code();
            errors.add(issue("COMPUTE_EXECUTION_FAILED", error));
        }
        Instant finished = clock.instant();
        return finishRun(request, output, log, started, finished, products,
                warnings, errors, exitCode);
    }

    private ComputeOutputs runWithLog(ComputeRequest request, JSONObject context,
                                      Path output, Path log) throws Exception {
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        try (OutputStream stream = Files.newOutputStream(
                log, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
        ); PrintStream logger = new PrintStream(stream, true, StandardCharsets.UTF_8)) {
            System.setOut(logger);
            System.setErr(logger);
            mapper.map(request, context);
            executor.execute(request);
            addPlotMetadata(output, request);
            return new ComputeProductInspector().inspect(output, request.title());
        } finally {
            System.setOut(originalOut);
            System.setErr(originalErr);
        }
    }

    private CliResponse finishRun(
            ComputeRequest request, Path output, Path log, Instant started, Instant finished,
            ComputeOutputs products, List<ValidationIssue> warnings,
            List<ValidationIssue> errors, int exitCode) {
        String status = exitCode == 0 ? "completed" : "failed";
        Path manifestPath = output.resolve("compute_manifest.json");
        ComputeManifest manifest = ComputeManifest.fromRun(
                request, status, started, finished, products,
                log.toAbsolutePath().normalize().toString(), warnings, errors
        );
        try {
            new ComputeManifestWriter().write(manifest, manifestPath);
        } catch (IOException error) {
            exitCode = CliExitCode.OUTPUT_ERROR.code();
            errors.add(issue("COMPUTE_MANIFEST_WRITE_FAILED", error));
        }
        JSONObject data = new JSONObject()
                .put("manifest_file", manifestPath.toAbsolutePath().normalize().toString())
                .put("outputs", products.toJson());
        String message = exitCode == 0 ? "Computation completed."
                : "Computation failed.";
        return CliResponse.computation(
                exitCode == 0 ? "success" : "error", exitCode, message,
                data, warnings, errors
        );
    }

    private void addPlotMetadata(Path output, ComputeRequest request)
            throws ComputeProductInspector.ProductValidationException {
        Path setting = output.resolve(request.title() + "_NGSViz_plotSetting.json");
        if (!Files.isRegularFile(setting)) return;
        try {
            JSONObject json = new JSONObject(new String(
                    Files.readAllBytes(setting), StandardCharsets.UTF_8
            ));
            JSONObject metadata = new JSONObject()
                    .put("sample_id", request.sampleId())
                    .put("sample_name", request.sampleName())
                    .put("group_name", request.groupName());
            json.put("run_metadata", metadata);
            Files.write(setting, json.toString(4).getBytes(StandardCharsets.UTF_8));
        } catch (Exception error) {
            throw new ComputeProductInspector.ProductValidationException(
                    new ValidationIssue("COMPUTE_OUTPUT_INVALID", "outputs",
                            "Cannot add plot-setting metadata: " + error.getMessage())
            );
        }
    }

    static Path outputDirectory(ComputeRequest request) {
        Path output = Paths.get(request.outputDir()).toAbsolutePath().normalize();
        return request.createResultFolder() ? output.resolve(request.title()) : output;
    }

    private static void invokeMainCalculator(ComputeRequest ignored) throws Exception {
        if ("exon".equals(InputParameterAttributes.interval_type)) {
            ExonModelData.getExonModelData(
                    InputParameterAttributes.tbl_name,
                    InputParameterAttributes.interval_type,
                    InputParameterAttributes.biotype
            );
        }
        MainCalculator.mainCalculator();
    }

    private ValidationIssue issue(String code, Exception error) {
        String message = error.getMessage() == null
                ? error.getClass().getSimpleName() : error.getMessage();
        return new ValidationIssue(code, null, message);
    }

    @FunctionalInterface
    interface RequestValidator {
        ComputeValidationResult validate(ComputeRequest request);
    }

    @FunctionalInterface
    interface RequestMapper {
        void map(ComputeRequest request, JSONObject context) throws Exception;
    }

    @FunctionalInterface
    interface ComputeExecutor {
        void execute(ComputeRequest request) throws Exception;
    }
}
