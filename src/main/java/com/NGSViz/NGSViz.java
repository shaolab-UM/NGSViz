package com.NGSViz;

import com.NGSViz.configSet.InputParameterAttributes;
import com.NGSViz.configSet.ProcessInputParameters;
import com.NGSViz.coverageCalculator.MainCalculator;
import com.NGSViz.sqldbOperate.exonMode.ExonModelData;
import java.io.IOException;
import java.net.URISyntaxException;

import static com.NGSViz.configSet.InputParameterAttributes.*;

/**
 * @author Benchen Ye
 * @create 2025-07-12:44
 * @function ngsViz
 */
public class NGSViz {
    public static void main(String[] args) throws IOException, URISyntaxException {
        if (CliOptions.handleInformationalOption(args)) {
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
}
