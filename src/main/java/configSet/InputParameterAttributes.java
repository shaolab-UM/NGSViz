package configSet;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Benchen Ye
 * @create 2024-11--20:10
 * @function set the attribute of input parameter and method to get their value
 */
public class InputParameterAttributes {
    public static String file_type = null;
    // necessary parameters
    public static String genome = null;
    public static String region_plot = null;
    public static String input_file = null;
    public static String output_path = null;
    // option parameters
    public static String DB_tpye = "ensembl";
    public static String analysis_type = "chipseq";
    // -1 is all gene in the database
    public static String gene_list = "-1";
    public static String analysis_title = "noname";
    public static int flank_region = 0;
    public static double flank_factor = 0.0;
    public static double robust = 0.0;
    public static double random_sampling_rate =0.0;
    public static int core_num = 0;
    public static String scaler_method = "spline";
    public static int chunk_size = 100;
    public static int min_mapq = 20;
    public static int frag_len = 150;
    public static int buf_size;
    public static String strand_spec = "both";
    public static int fi_tag = 0;

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
    public static Map<String, String> getStringParameter2ValueMap (){
        Map<String, String> para_value_map = new HashMap<String, String>();
        para_value_map.put("file_type", file_type);
        para_value_map.put("genome", genome);
        para_value_map.put("region_plot", region_plot);
        para_value_map.put("input_file", input_file);
        para_value_map.put("output_path", output_path);
        para_value_map.put("db_tpye", DB_tpye);
        para_value_map.put("analysis_type", analysis_type);
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

    // get the value of parameter
    public static String getStringParameterValue (String parameter_name){
        Map<String, String> str_para_value_map = getStringParameter2ValueMap();
        String parameter_value = str_para_value_map.get(parameter_name);
        return parameter_value;
    }
    public static int getIntParameterValue (String parameter_name){
        Map<String, Integer> str_para_value_map = getInterParameter2ValueMap();
        int parameter_value = str_para_value_map.get(parameter_name);
        return parameter_value;
    }
    public static Double getDoubleParameterValue (String parameter_name){
        Map<String, Double> str_para_value_map = getDoubleParameter2ValueMap();
        Double parameter_value = str_para_value_map.get(parameter_name);
        return parameter_value;
    }

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
    // option
    public static String getFileType() {
        return file_type;
    }
    public static String getAnalysisType() {
        return analysis_type;
    }
    public static String getGeneList() {
        return gene_list;
    }
    public static String getAnalysisTitle() {
        return analysis_title;
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
