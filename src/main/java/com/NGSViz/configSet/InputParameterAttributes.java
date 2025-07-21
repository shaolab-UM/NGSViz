package com.NGSViz.configSet;

import htsjdk.samtools.SAMFileHeader;

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
    public static String title = null; //-T
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
    public static String sys_config_path = ""; // -CP //
    public static List<String> gene_list = new ArrayList<>(); // -E
    public static List<String> analysis_title = new ArrayList<>();
    public static List<String> sample_list = new ArrayList<>();
    public static List<String> group_list = new ArrayList<>();
    public static String db_path = "";
    public static String input_db_path = ""; // merge db for input db path
    public static String bedDB_path = null; // -BD
    // option parameters
    public static String DB_type = null; //-D
    // all is all gene in the database
    public static String genes = "all"; // -E
    public static String analysis_type = "transcript"; // -A
    public static String biotype = "protein_coding"; // -F
    public static int flank_region = 0; //-L
    public static double flank_factor = 0.0; // -N
    public static double robust = 0.0; //-RB
    public static double random_sampling_rate =0.0;
    public static double scale_ratio = 1.0;//-S
    public static int core_num = 1; //-P
    public static String scaler_method = "bin"; //-AL
    public static int chunk_size = 100; //-CS
    public static int min_mapq = 20; //-MQ
    public static int frag_len = 150; //-FL
    public static String strand_spec = "both"; //-SS
    public static int fi_tag = 0; // -FI
    public static boolean bamChromLabStartWithChr = true;
    public static SAMFileHeader header;

    public static String getGenome() {
        return genome;
    }
}
