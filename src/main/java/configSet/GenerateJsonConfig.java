package configSet;

import org.json.JSONArray;
import org.json.JSONObject;
import utils.DirectoryChecker;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author Benchen Ye
 * @create 2024-11--19:08
 * @function generate the config file
 */
public class GenerateJsonConfig extends InputParameterAttributes {
    public static void generateJsonConfig(String out_path, String config_name) {
        // check whether the directory of out path exist
        DirectoryChecker.checkDirectoryExist(out_path);
        //Path config_path = Paths.get(out_path, config_name);
        // remove the '\' in the end of path
        String cleaned_path = DirectoryChecker.removeTrailingSlash(out_path);
        String config_path = cleaned_path + "/" + config_name;
        System.out.println("config_path: " + config_path);
        JSONObject config_obj = transformParas2JsonConfig();
        try {
            // identify config file path and name
            FileWriter config_file = new FileWriter(config_path);
            // transform the JSONObject into character and write in file
            // // Indentation is four spaces.
            config_file.write(config_obj.toString(4));
            config_file.close();
            System.out.println(config_path + " : JSON config file has been generated!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static JSONObject transformParas2JsonConfig() {
        // create JSONObject to save config
        JSONObject config = new JSONObject();
        // create "mandatory_parameters" JSONObject
        JSONObject mandatoryParameters = new JSONObject();
        JSONArray bam_array = new JSONArray();
        bam_array = buildJsonListConfig(bam_array, bam_list);
        mandatoryParameters.put("bam_file", bam_array);
        mandatoryParameters.put("genome", genome);
        mandatoryParameters.put("output_name", output_path);
        mandatoryParameters.put("region_plot", region_plot);
        config.put("mandatory_parameters", mandatoryParameters);

        // create "basis_parameters" JSONObject
        JSONObject basis_parameters = new JSONObject();
        basis_parameters.put("database", DB_tpye);
        basis_parameters.put("analysis_type", analysis_type);
        //basis_parameters.put("species", species);
        //basis_parameters.put("coord_refDB", refname);
        JSONArray gene_listArray = new JSONArray();
        gene_listArray = buildJsonListConfig(gene_listArray, gene_list);
        basis_parameters.put("gene_list", gene_listArray);
        config.put("basis_parameters", basis_parameters);

        // create "coverage_calculator" JSONObject
        JSONObject coverage_calculator = new JSONObject();
        coverage_calculator.put("flank_size", flank_region);
        coverage_calculator.put("flank_factor", flank_factor);
        coverage_calculator.put("robust", robust);
        coverage_calculator.put("core_num", core_num);
        coverage_calculator.put("scaler_method", scaler_method);
        coverage_calculator.put("min_mapq", min_mapq);
        coverage_calculator.put("frag_len", frag_len);
        coverage_calculator.put("buf_size", buf_size);
        coverage_calculator.put("strand_spec", strand_spec);
        coverage_calculator.put("fi_tag", fi_tag);
        coverage_calculator.put("interval_type", interval_type);
        coverage_calculator.put("random_sampling_rate", random_sampling_rate);
        coverage_calculator.put("chunk_size", chunk_size);
        JSONArray region_labelsArray = new JSONArray();
        region_labelsArray = buildJsonListConfig(region_labelsArray, region_labels);
        coverage_calculator.put("region_labels", region_labelsArray);
        config.put("coverage_calculator", coverage_calculator);

        // create "plot_parameters" JSONObject
        JSONObject plotParameters = new JSONObject();
        plotParameters.put("num_datapoints", num_datapoints);
        plotParameters.put("middle_datapoints", middle_points);
        plotParameters.put("flanking_region_datapoints", flank_points);
        // create JSONArray to save list data
        JSONArray plotTitle_array = new JSONArray();
        plotTitle_array = buildJsonListConfig(plotTitle_array, analysis_title);
        plotParameters.put("plot_title", plotTitle_array);
        config.put("plot_parameters", plotParameters);
        return config;
    }
    public static JSONArray buildJsonListConfig(JSONArray list_array, List<String> str_list) {
        // create JSONArray to save list data
        JSONArray plotTitle_array = new JSONArray();
        for (int i = 0; i < str_list.size(); i++) {
            plotTitle_array.put(str_list.get(i));
        }
        return plotTitle_array;
    }
}
