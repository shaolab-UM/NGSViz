package com.NGSViz.configSet;

import java.util.*;

/**
 * @author Benchen Ye
 * @create 2024-11--20:10
 * @function set the attribute of input parameter and method to get their value
 */
public class InputParameterAttributes {
    public static String file_type = null;
    // necessary parameters
    public static String genome = null; //-G
    public static String region_type = null; //-R
    public static String input_file = null; // -C
    public static String output_path = null; //-O
    //
    public static String version_num = "1.0";
    public static String refname = null;
    public static String species = null;
    public static String tbl_name = null;
    public static List<String> bam_list = new ArrayList<>();
    public static int buf_size;
    public static List<String> region_labels = new ArrayList<>();
    public static String interval_type = null;
    public static int num_datapoints = 100;
    public static int middle_points = 0;
    public static int flank_points = 0;
    public static String sys_config_name = "NGSViz_setting.json";
    public static String config_name = "NGSVir_running_config.json";
    public static String sys_config_path = "";
    public static List<String> gene_list = new ArrayList<>(); // -E
    public static List<String> analysis_title = new ArrayList<>();
    public static String db_path = "";
    public static String input_db_path = "";
    //public static String avg_mat_file = "";
    //public static String avg_mat_file = "";
    //public static String err_mat_file = "";
    // option parameters
    public static String DB_type = null; //-D
    // all is all gene in the database
    public static String genes = "all"; // -E
    public static String title = "AverageCoverage"; //-T
    public static String analysis_type = "transcript"; // -A
    public static String biotype = "protein_coding"; // -F
    public static int flank_region = 0; //-L
    public static double flank_factor = 0.0; // -N
    public static double robust = 0.0; //-RB
    public static double random_sampling_rate =0.0; //-S
    public static int core_num = 1; //-P
    public static String scaler_method = "bin"; //-AL
    public static int chunk_size = 100; //-CS
    public static int min_mapq = 20; //-MQ
    public static int frag_len = 150; //-FL
    public static String strand_spec = "both"; //-SS
    public static int fi_tag = 0; // -FI

    //
    public static Map<String, Integer> getInterParameter2ValueMap (){
        Map<String, Integer> para_value_map = new HashMap<String, Integer>();
        para_value_map.put("flank_region", flank_region);
        para_value_map.put("core_num", core_num);
        para_value_map.put("chunk_size", chunk_size);
        para_value_map.put("min_mapq", min_mapq);
        para_value_map.put("frag_len", frag_len);
        para_value_map.put("buf_size", buf_size);
        para_value_map.put("fi_tag", fi_tag);
        return para_value_map;
    }
    public static Map<String, Object> getStringParameter2ValueMap (){
        Map<String, Object> para_value_map = new HashMap<String, Object>();
        para_value_map.put("genome", genome);
        para_value_map.put("region_plot", region_type);
        para_value_map.put("input_file", input_file);
        para_value_map.put("output_path", output_path);
        para_value_map.put("db_tpye", DB_type);
        para_value_map.put("analysis_type", analysis_type);
        para_value_map.put("biotype", biotype);
        para_value_map.put("gene_list", gene_list);
        para_value_map.put("analysis_title", analysis_title);
        para_value_map.put("scaler_method", scaler_method);
        para_value_map.put("strand_spec", strand_spec);
        return para_value_map;
    }
    public static Map<String, Double> getDoubleParameter2ValueMap (){
        Map<String, Double> para_value_map = new HashMap<String, Double>();
        para_value_map.put("flank_factor", flank_factor);
        para_value_map.put("robust", robust);
        para_value_map.put("random_sampling_rate", random_sampling_rate);
        return para_value_map;
    }
    public static String getGenome() {
        return genome;
    }
}
