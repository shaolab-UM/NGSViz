package com.NGSViz.cli;

import com.NGSViz.compute.ComputeRequest;
import com.NGSViz.compute.ComputeRequestFormatException;
import com.NGSViz.compute.ComputeRequestReader;
import com.NGSViz.compute.ComputeValidationResult;
import com.NGSViz.compute.ComputeValidator;
import org.json.JSONObject;

import java.nio.file.Path;
import java.nio.file.Paths;

/** Executes native read-only computation validation. */
final class ValidateComputeCommand {
    CliResponse execute(String requestArgument) {
        Path requestFile = Paths.get(requestArgument).toAbsolutePath().normalize();
        JSONObject data = new JSONObject().put("request_file", requestFile.toString());
        try {
            ComputeRequest request = new ComputeRequestReader().read(requestFile);
            ComputeValidationResult result = new ComputeValidator().validate(request);
            mergeContext(data, result.context());
            return CliResponse.validation(
                    CliCommand.VALIDATE_COMPUTE.commandName(), data, result
            );
        } catch (ComputeRequestFormatException error) {
            return CliResponse.requestError(
                    CliCommand.VALIDATE_COMPUTE.commandName(),
                    "Compute request format is invalid.", data, error.issues()
            );
        }
    }

    private void mergeContext(JSONObject destination, JSONObject context) {
        for (String key : context.keySet()) destination.put(key, context.get(key));
    }
}
