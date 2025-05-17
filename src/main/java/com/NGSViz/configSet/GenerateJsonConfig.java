package com.NGSViz.configSet;

import org.json.JSONArray;
import org.json.JSONObject;
import com.NGSViz.utils.DirectoryChecker;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Benchen Ye
 * @ create 2024-11--19:08
 * @ function generate the config file
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

    // generate the plot parameters
    public static void generatePlotJsonConfig(String[] args) {

    }

    public static void generateJsonConfig2(String out_path, String config_name, JSONObject config_obj) {
        // check whether the directory of out path exist
        DirectoryChecker.checkDirectoryExist(out_path);
        //Path config_path = Paths.get(out_path, config_name);
        // remove the '\' in the end of path
        String cleaned_path = DirectoryChecker.removeTrailingSlash(out_path);
        String config_path = cleaned_path + "/" + config_name;
        System.out.println("config_path: " + config_path);
        //JSONObject config_obj = transformParas2JsonConfig();
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
        List<String> sample_color = Arrays.asList(
                // default color panel is set2
                "#66C2A5", "#FC8D62", "#8DA0CB", "#E78AC3", "#A6D854", "#FFD92F"
        );
        List<String> split_color = Arrays.asList(
                // default color panel is nejm
                "#BC3C29FF", "#0072B5FF", "#E18727FF", "#20854EFF", "#7876B1FF", "#6F99ADFF"
        );

        // Define the structure of the JSON data using Maps
        JSONObject config = new JSONObject();
        // versionNum object
        JSONObject versionNum = new JSONObject();
        versionNum.put("version", version_num);
        config.put("versionParameters", versionNum);
        // create the plot_parameters JSONObject
        JSONObject plot_parameters = new JSONObject();
        plot_parameters.put("plot_title", analysis_title);
        plot_parameters.put("flank_size", flank_region);
        JSONArray region_labelsArray = new JSONArray();
        region_labelsArray = buildJsonListConfig(region_labelsArray, region_labels);
        plot_parameters.put("region_labels", region_labelsArray);
        plot_parameters.put("interval_type", interval_type);
        plot_parameters.put("num_datapoints", num_datapoints);
        plot_parameters.put("middle_datapoints", middle_points);
        plot_parameters.put("flanking_region_datapoints", flank_points);
        // create JSONArray to save list data
        JSONArray sample_array = new JSONArray();
        sample_array = buildJsonListConfig(sample_array, sample_list);
        plot_parameters.put("sample_list", sample_array);
        config.put("plot_parameters", plot_parameters);

        // create the output_paras JSONObject
        JSONObject output_paras = new JSONObject();
        output_paras.put("output_path", output_path);
        output_paras.put("width", sample_list.size()*10);
        output_paras.put("high", 12);
        output_paras.put("dpi", 300);
        output_paras.put("file_type", "tiff");
        JSONArray tbr_labelsArray = new JSONArray();
        tbr_labelsArray = buildJsonNumListConfig(tbr_labelsArray, Arrays.asList(1,3));
        output_paras.put("top_bottom_ratio", tbr_labelsArray);
        config.put("output_paras", output_paras);

        // theme_paras object
        JSONObject theme_paras = new JSONObject();
        theme_paras.put("line_size", 0.8);
        theme_paras.put("dash_size", 0.6);
        theme_paras.put("dash_color", "black");
        // split default color
        JSONArray splitColor_labelsArray = new JSONArray();
        splitColor_labelsArray = buildJsonListConfig(splitColor_labelsArray, split_color);
        theme_paras.put("split_color", splitColor_labelsArray);
        // sample default color
        JSONArray sampleColor_labelsArray = new JSONArray();
        sampleColor_labelsArray = buildJsonListConfig(sampleColor_labelsArray, sample_color);
        theme_paras.put("sample_color", sampleColor_labelsArray);
        theme_paras.put("border_size", 1.2);
        theme_paras.put("text_family", "Arial");
        theme_paras.put("text_face", "bold");
        theme_paras.put("axis_text_x_size", 10);
        theme_paras.put("axis_text_y_size", 10);
        theme_paras.put("legend_text_size", 10);
        theme_paras.put("ystrip_text_size", 8);
        theme_paras.put("title_size", 11);
        theme_paras.put("legend_size", 10);
        theme_paras.put("title_color", "black");
        theme_paras.put("title_hjust", 0.5);
        config.put("theme_paras", theme_paras);

        // split_paras object
        Map<String, Object> split_paras = new LinkedHashMap<>();
        split_paras.put("split_mode", "cluster");
        split_paras.put("cluster_num", 1);
        split_paras.put("hc_m", "ward.D");
        split_paras.put("dist_m", "euclidean");
        // gene list label
        JSONArray genelist_labelsArray = new JSONArray();
        genelist_labelsArray = buildJsonListConfig(genelist_labelsArray, Arrays.asList("all"));
        split_paras.put("sub_gene_list", genelist_labelsArray);
        split_paras.put("gene_list_file", "");
        config.put("split_paras", split_paras);
        return config;
    }

    /*public static JSONObject transformParas2JsonConfig() {
        // create JSONObject to save config
        JSONObject config = new JSONObject();
        // create "version"
        JSONObject versionParameters = new JSONObject();
        versionParameters.put("version", version_num);
        config.put("versionParameters", versionParameters);
        // create "mandatory_parameters" JSONObject
        JSONObject mandatoryParameters = new JSONObject();
        JSONArray bam_array = new JSONArray();
        bam_array = buildJsonListConfig(bam_array, bam_list);
        mandatoryParameters.put("bam_file", bam_array);
        mandatoryParameters.put("genome", genome);
        mandatoryParameters.put("output_name", output_path);
        mandatoryParameters.put("region_plot", region_type);
        config.put("mandatory_parameters", mandatoryParameters);

        // create "basis_parameters" JSONObject
        JSONObject basis_parameters = new JSONObject();
        basis_parameters.put("database", DB_type);
        basis_parameters.put("analysis_type", analysis_type);
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
        // create JSONArray to save list data
        JSONArray sample_array = new JSONArray();
        sample_array = buildJsonListConfig(sample_array, sample_list);
        plotParameters.put("sample_list", sample_array);
        config.put("plot_parameters", plotParameters);

        return config;
    }*/

    public static JSONArray buildJsonListConfig(JSONArray list_array, List<String> str_list) {
        // create JSONArray to save list data
        JSONArray plotTitle_array = new JSONArray();
        for (int i = 0; i < str_list.size(); i++) {
            plotTitle_array.put(str_list.get(i));
        }
        return plotTitle_array;
    }
    public static JSONArray buildJsonNumListConfig(JSONArray list_array, List<Integer> int_list) {
        // create JSONArray to save list data
        JSONArray plotTitle_array = new JSONArray();
        for (int i = 0; i < int_list.size(); i++) {
            plotTitle_array.put(int_list.get(i));
        }
        return plotTitle_array;
    }
}
