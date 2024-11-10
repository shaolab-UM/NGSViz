package configSet;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Benchen Ye
 * @create 2024-11--19:55
 */
public class GetCommandParameter extends InputParameterAttributes {
    private static String genome = null;
    private static String region_plot = null;
    private static String input_file = null;
    private static String file_type = null;
    private static String output_path = null;
    private static String DB_tpye = "ensembl";
    private static String analysis_type = "chipseq";
    // -1 is all gene in the database
    private static String gene_list = "-1";
    private static String analysis_title = "noname";
    private static int flank_region = 0;
    private static double flank_factor = 0.0;
    private static double robust = 0.0;
    private static double random_sampling_rate =0.0;
    private static int core_num = 0;
    private static String scaler_method = "spline";
    private static int chunk_size = 100;
    private static int min_mapq = 20;
    private static int frag_len = 150;
    private static int buf_size;
    private static String strand_spec = "both";
    private static int fi_tag = 0;

    public static void main(String @NotNull [] args) {
        // Simulate command line arguments --- test
        args = new String[]{"-G", "hg19", "-R", "genebody", "-C",
                "/Users/bencheye/myProj/ngsPlot/Data/hesc.RNAseq.1M.bam",
                "-O", "/Users/bencheye/myProj/ngsPlot/Output/hesc.RNAseq.1M",
        };
        ProcessInputParameters.processInputParameter(args);
        // Check necessary parameters.
        //checkFlankRegionValue(res_out);
        //checkAnalysisMethod(res_out);
        //checkValue01(converted_number, "-N");
        //checkValue01(converted_number, "-RB");
        //checkValue01(converted_number, "-S");
        //checkValueGreater0(converted_number, "-P");
        //checkScaleMethod(res_out);
        //checkValueGreater0(converted_number, "-CS");
        //checkValueGreater0(converted_number, "-MQ");
        //checkValueGreater0(converted_number, "-FL");
        //checkStrandSpecificValue(res_out);
        //checkValueGreater0(converted_number, "-FI");
        //checkNecessaryPara();
        // set the default value for option parameters.
        //setDefaulatOptionParameter();
    }




}
