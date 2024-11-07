package configSet;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Benchen Ye
 * @create 2024-11--19:55
 */
public class GetCommandParameter {
    private static String genome = null;
    private static String region_plot = null;
    private static String input_file = null;
    private static String output_path = null;
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
        getInputParameter(args);
    }
    public static void getInputParameter(String[] args) {
        // Parse by traversing the command line argument array.
        for (int i = 0; i < args.length; i = i + 2) {
            String res_out = null;
            // necessary parameters
            res_out = getParameterValue("-G", args, i);
            if (res_out != null) {genome = res_out;}
            res_out = getParameterValue("-R", args, i);
            if (res_out != null) {
                checkFlankRegionValue(res_out);
                region_plot = res_out;
            }
            res_out = getParameterValue("-C", args, i);
            if (res_out != null) {input_file = res_out;}
            res_out = getParameterValue("-O", args, i);
            if (res_out != null) {output_path = res_out;}

            // Option parameters
            // Flanking region size
            res_out = getParameterValue("-L", args, i);
            if (res_out != null) {
                int converted_number = Integer.parseInt(res_out);
                flank_region = converted_number;
            }
            // Flanking size factor
            res_out = getParameterValue("-N", args, i);
            if (res_out != null) {
                double converted_number = Double.parseDouble(res_out);
                checkValue01(converted_number, "-N");
                flank_factor = converted_number;
            }
            // Robust statistics
            res_out = getParameterValue("-RB", args, i);
            if (res_out != null) {
                double converted_number = Double.parseDouble(res_out);
                checkValue01(converted_number, "-RB");
                robust = converted_number;
            }
            // Random sampling rate
            res_out = getParameterValue("-S", args, i);
            if (res_out != null) {
                double converted_number = Double.parseDouble(res_out);
                checkValue01(converted_number, "-S");
                random_sampling_rate = converted_number;
            }
            // Set cores number
            res_out = getParameterValue("-P", args, i);
            if (res_out != null) {
                int converted_number = Integer.parseInt(res_out);
                checkValueGreater0(converted_number, "-P");
                core_num = converted_number;
            }
            // Algorithm for coverage vector normalization (scale data)
            res_out = getParameterValue("-AL", args, i);
            if (res_out != null) {scaler_method = res_out;}
            // Gene chunk size
            res_out = getParameterValue("-CS", args, i);
            if (res_out != null) {
                int converted_number = Integer.parseInt(res_out);
                checkValueGreater0(converted_number, "-CS");
                chunk_size = converted_number;
            }
            // Mapping quality cutoff
            res_out = getParameterValue("-MQ", args, i);
            if (res_out != null) {
                int converted_number = Integer.parseInt(res_out);
                checkValueGreater0(converted_number, "-MQ");
                min_mapq = converted_number;
            }
            // Fragment length
            res_out = getParameterValue("-FL", args, i);
            if (res_out != null) {
                int converted_number = Integer.parseInt(res_out);
                checkValueGreater0(converted_number, "-FL");
                frag_len = converted_number;
            }
            // Strand-specific coverage
            res_out = getParameterValue("-SS", args, i);
            if (res_out != null) {
                checkStrandSpecificValue(res_out);
                strand_spec = res_out;
            }
            // Image output forbidden tag
            res_out = getParameterValue("-FI", args, i);
            if (res_out != null) {
                int converted_number = Integer.parseInt(res_out);
                checkValueGreater0(converted_number, "-FI");
                fi_tag = converted_number;
            }
        }
        // Check necessary parameters.
        checkNecessaryPara();
        // set the default value for option parameters.
        setDefaulatOptionParameter();
    }

    // check value
    public static void checkValue01 (double check_value, String parameter_name) {
        if(check_value < 0 || check_value > 1) {
            System.out.println(parameter_name + " must be between 0 and 1!");
            System.exit(-1);
        }
    }
    public static void checkValueGreater0 (double check_value, String parameter_name) {
        if(check_value < 0 ) {
            System.out.println(parameter_name + " must be greater than 0!");
            System.exit(-1);
        }
    }
    public static void checkStrandSpecificValue (String check_value) {
        String[] spec_allowed = {"both", "same", "opposite"};
        boolean isInArray = false;
        for (String element : spec_allowed) {
            if (element.equals(check_value)) {
                isInArray = true;
                break;
            }
        }
        if (!isInArray) {
            System.out.println(check_value + " not allow for -SS, ");
            System.out.println("please input `both`, `same` or `opposite` for `Strand-specific coverage`");
            System.exit(-1);
        }
    }
    public static void checkFlankRegionValue (String flank_region) {
        Map<String, Integer> region_FR_map = getRegion2FRMap();
        boolean value_legal = region_FR_map.containsKey(flank_region);
        if (!value_legal) {
            System.out.println("-R, flank_region : `" + flank_region + "` is not a legal value.");
            System.out.println("Please input tss, tes, genebody, exon, cgi, enhancer, dhs OR bed!");
            System.exit(-1);
        }
    }
    // set the default value for option parameters.
    public static void setDefaulatOptionParameter() {
        // Flanking region size
        if (flank_region == 0) {
            flank_region = getFlankRegion(region_plot);
            System.out.println("flank_region: " + flank_region);
        }
        // Flanking size factor
        if (flank_factor == 0.0){
            System.out.println("flank_factor: " + flank_factor);
        }
        if (robust == 0.0){
            System.out.println("robust: " + robust);
        }
        if (random_sampling_rate == 1.0){
            System.out.println("random_sampling_rate: " + random_sampling_rate);
        }
        if (core_num == 0){
            System.out.println("core_num: " + core_num);
        }
        if (scaler_method == "spline") {
            System.out.println("scaler_method: " + scaler_method);
        }
        if (chunk_size == 100){
            System.out.println("chunk_size: " + chunk_size);
        }
        if (min_mapq == 20){
            System.out.println("min_mapq: " + min_mapq);
        }
        if (frag_len == 150){
            System.out.println("frag_len: " + frag_len);
        }
        buf_size = frag_len;
        System.out.println("buf_size: " + buf_size);
        if (strand_spec == "both") {
            System.out.println("strand_spec: " + strand_spec);
        }
        if (fi_tag == 0){
            System.out.println("fi_tag: " + fi_tag);
        }
    }

    // get the value of input parameters
    public static String getParameterValue(String parameter, String [] args, int index) {
        String out_value = null;
        if (parameter.equals(args[index])) {
            if (index + 1 < args.length) {
                out_value = args[index + 1];
                System.out.println(parameter + ": " + out_value);
            } else {
                System.out.println("Parameter " + parameter + "without value!");
                System.exit(-1);
            }
        }
        return out_value;
    }

    // check necessary parameters
    public static void checkNecessaryPara(){
        if (genome == null) {
            System.out.println("There is not a parameter -G, please check!");
            return;
        }
        if (region_plot == null) {
            System.out.println("There is not a parameter -R, please check!");
            return;
        }
        if (input_file == null) {
            System.out.println("There is not a parameter -C, please check!");
            return;
        }
        if (output_path == null) {
            System.out.println("There is not a parameter -O, please check!");
            return;
        }
    }

    // default value of option parameters
    public static Map<String, Integer> getRegion2FRMap() {
        int[] flank_region = {2000, 2000, 2000, 500, 500, 1500, 1000, 1000};
        String[] region_plot = {"tss", "tes", "genebody", "exon", "cgi", "enhancer", "dhs", "bed"};
        Map<String, Integer> data_map = new HashMap<>();
        for (int i = 0; i < flank_region.length; i++) {
            data_map.put(region_plot[i], flank_region[i]);
        }
        return data_map;
    }
    public static int getFlankRegion (String region_plot) {
        checkFlankRegionValue(region_plot);
        Map<String, Integer> region_FR_map = getRegion2FRMap();
        int flank_region = region_FR_map.get(region_plot);
        return flank_region;
    }

    // get the value of parameter
    public static String getGenome() {
        return genome;
    }
    public static String getRegion2Plot() {
        return region_plot;
    }
    public static String getInputFile() {
        return input_file;
    }
    public static String getOutputPath() {
        return output_path;
    }
    public static int getFlankRegion() {
        return flank_region;
    }
    public static double getFlankFactor() {
        return flank_factor;
    }
    public static double getRobustValue() {
        return robust;
    }
    public static double getRandomSamplingRate() {
        return random_sampling_rate;
    }
    public static int getCoreNum() {
        return core_num;
    }
    public static String getScalerMethod() {
        return scaler_method;
    }
    public static int getChunkSize() {
        return chunk_size;
    }
    public static int getMinMapq() {
        return min_mapq;
    }
    public static int getFragLen() {
        return frag_len;
    }
    public static int getBufSize() {
        return buf_size;
    }
    public static String getStrandSpec() {
        return strand_spec;
    }
    public static int getFiTag() {
        return fi_tag;
    }
}
