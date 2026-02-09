package com.NGSViz.configSet;

import com.NGSViz.sqldbOperate.QueryDBUniqueItem;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Benchen Ye
 * @ create 2024-11--20:09
 * @ function check whether is a legal value for input parameter
 */
public class CheckLegal2InputParameter extends InputParameterAttributes {
    public static void checkLegal2InputParameter() {
        System.out.println("Start check the legality of all input parameters!");
        System.out.println("---------");
        // check necessary parameters
        checkNecessaryPara();
        // check input file path
        CheckFilePath(input_file);
        // check whether the input parameter is legal value
        checkQueryDBUniqLegal(genome, "Genome");
        region_type = region_type.toLowerCase();
        checkQueryDBUniqLegal(region_type, "Region");
        if (DB_type != null){
            checkQueryDBUniqLegal(DB_type, "DB");
        }
        checkAnalysisType(analysis_type);
        checkValueGreater0(flank_region, "-F");
        checkValue01(flank_factor, "-N");
        checkValue01(robust, "-RB");
        //checkValue01(scale_ratio, "-S");
        checkValueGreater0(core_num, "-P");
        checkBinMethod(bin_method);
        checkValueGreater0(BATCH_SIZE, "-BS");
        checkValueGreater0(min_mapq, "-MQ");
        checkValueGreater0(frag_len, "-FL");
        checkStrandSpecificValue(strand_spec);
        checkValueGreater0(fi_tag, "-FI");
        System.out.println("All input parameters have been checked!");
        System.out.println("---------");
    }
    //
    // check whether the path of input file or directory exist
    public static void CheckFilePath (String file_path) {
        List<String> legal_extension = new ArrayList<>();
        legal_extension.add("txt");
        legal_extension.add("csv");
        legal_extension.add("bam");

        String[] files = file_path.split(":");
        for (String file_ath : files) {
            File file = new File(file_ath);
            if (file.exists()) {
                System.out.println("Input file path : " + file_ath);
            } else {
                System.out.println("Error: Input file or directory not exists !");
                System.out.println("Input file path : " + file_ath);
                System.exit(-1);
            }
            String file_type = getFileExtension(file_ath);
            if (file_type.isEmpty()) {
                System.out.println("Error: There is no extension file for input file!");
                System.out.println("Input file path : " + file_ath);
                System.exit(-1);
            } else if (! legal_extension.contains(file_type)) {
                System.out.println("Error: Input file or not in `bam`, `txt` or `csv` !");
                System.out.println("Input file path : " + file_ath);
                System.exit(-1);
            }
        }

    }
    // get the extension name of file
    public static String getFileExtension(String file_path) {
        String file_type = "";
        int last_index = file_path.lastIndexOf('.');
        if (last_index > 0 && last_index < file_path.length() - 1) {
            file_type = file_path.substring(last_index + 1);
            System.out.println("file_type: " + file_type);
            return file_type;
        }
        return file_type;
    }

    public static void checkQueryDBUniqLegal(String str_name, String query_key){
        List<String> query_res = QueryDBUniqueItem.getDBQueryUniqueItem(query_key);
        if (! query_res.contains(str_name)) {
            System.out.println(str_name + " is a illegal character!");
            System.out.println("Please enter a valid query key as follow!");
            System.out.println(query_res);
            System.exit(-1);
        }
    }
    // check value
    public static void checkBinMethod (String check_value){
        String [] legal_list = {"mean", "median", "max"};
        boolean value = check_value.equals(legal_list[0]) || check_value.equals(legal_list[1]) || check_value.equals(legal_list[2]);
        if (!value){
            System.out.println("Your input paramter -BM `" + check_value +  "` is illegal item!");
            System.out.println("-BM must be `mean` , `median`, `max`!");
            System.exit(-1);
        }
    }
    private static void checkAnalysisType (String check_value){
        String [] legal_list = {"exon", "transcript"};
        boolean value = check_value.equals(legal_list[0]) || check_value.equals(legal_list[1]);
        if (!value){
            System.out.println("Your input paramter -A `" + check_value +  "` is illegal item!");
            System.out.println("-A must be `transcript` or `exon`!");
        }
    }
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

    // default value of option parameters
    public static Map<String, Integer> getRegion2FRMap() {
        int[] flank_region = {2000, 2000, 2000, 2000, 2000, 1500, 1000, 1000};
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
        return region_FR_map.get(region_plot);
    }
    // check necessary parameters
    public static void checkNecessaryPara(){
        if (genome == null) {
            throw new IllegalArgumentException("Missing required parameter -G. " +
                    "Please specify the genome database used in the analysis.");
        }
        if (region_type == null) {
            throw new IllegalArgumentException("Missing required parameter -R. " +
                    "Please specify the region type .");
        }
        if (input_file == null) {
            throw new IllegalArgumentException("Missing required parameter -I. " +
                    "Please specify the input file, bam file path.");
        }
        if (output_path == null) {
            throw new IllegalArgumentException("Missing required parameter -O. " +
                    "Please specify the output file path.");
        }
        if (title == null) {
            throw new IllegalArgumentException("Missing required parameter -T. " +
                    "Please specify the analysis title.");
        }
    }
}
