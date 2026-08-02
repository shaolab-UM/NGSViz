package com.NGSViz;

import com.NGSViz.cli.CliDispatcher;
import com.NGSViz.cli.CliExitCode;
import com.NGSViz.cli.CliResponse;
import com.NGSViz.cli.LegacyComputeRequestAdapter;
import com.NGSViz.cli.RunComputeCommand;
import com.NGSViz.compute.ComputeRequest;
import com.NGSViz.configSet.InputParameterAttributes;
import com.NGSViz.configSet.ProcessInputParameters;
import com.NGSViz.coverageCalculator.MainCalculator;
import com.NGSViz.sqldbOperate.exonMode.ExonModelData;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import static com.NGSViz.configSet.InputParameterAttributes.*;

/**
 * @author Benchen Ye
 * @create 2025-07-12:44
 * @function ngsViz
 */
public class NGSViz {
    public static void main(String[] args) throws IOException, URISyntaxException {
        if (CliDispatcher.shouldDispatch(args)) {
            int exitCode = new CliDispatcher(System.out).dispatch(args);
            if (exitCode != CliExitCode.SUCCESS.code()) {
                System.exit(exitCode);
            }
            return;
        }
        if (CliOptions.handleInformationalOption(args)) {
            return;
        }

        if (!Arrays.asList(args).contains("-DB")) {
            runLegacyCompute(args);
            return;
        }

        long startTimeMillis = System.currentTimeMillis();
        // process the parameters
        ProcessInputParameters.processInputParameter(args);
        // exonMode for exon
    /*
    Here, ExonModelData is used to obtain the lengths, starting sites, and other information of all different transcripts.
    * */
        if (InputParameterAttributes.interval_type.equals("exon")) {
            ExonModelData.getExonModelData(InputParameterAttributes.tbl_name,
                    InputParameterAttributes.interval_type,
                    InputParameterAttributes.biotype);
        }
        MainCalculator.mainCalculator();

        long endTimeMillis = System.currentTimeMillis();
        long durationNano = endTimeMillis - startTimeMillis;
        double durationSeconds = durationNano / 1_000.0;
        System.out.println("----------------------------------------");
        System.out.println("ngsViz analysis has completed!");
        System.out.println("The result is saved in: " + output_path);
        System.out.println("Running time: " + durationSeconds);
        System.out.println("----------------------------------------");
    }

    private static void runLegacyCompute(String[] args) throws IOException {
        CliResponse response;
        try {
            List<ComputeRequest> requests = new LegacyComputeRequestAdapter().fromArgsList(args);
            RunComputeCommand command = new RunComputeCommand();
            response = null;
            for (ComputeRequest request : requests) {
                response = command.execute(request);
                if (response.exitCode() != CliExitCode.SUCCESS.code()) break;
            }
            if (response == null) {
                response = CliResponse.error(
                        "run-compute", "Legacy input contains no samples.",
                        "LEGACY_INPUT_EMPTY"
                );
            }
        } catch (Exception error) {
            response = CliResponse.error(
                    "run-compute", error.getMessage() == null
                            ? error.getClass().getSimpleName() : error.getMessage(),
                    "CLI_ARGUMENT_ERROR"
            );
        }
        System.out.println(response.toJson().toString());
        if (response.exitCode() != CliExitCode.SUCCESS.code()) {
            System.exit(response.exitCode());
        }
    }
}
