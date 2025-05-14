package com.NGSViz.utils;

import com.NGSViz.configSet.InputParameterAttributes;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * @author Benchen Ye
 * @create 2025-05--PM8:44
 */
public class GeneratePlotParasJson extends InputParameterAttributes {

    /*public static void main(String[] args) {
        generatePlotParasJson(out_path);
    }*/
    public static void generatePlotParasJson(String out_path) {
        String plot_json_name = "NGSViz_plotSetting.json";
        DirectoryChecker.checkDirectoryExist(out_path);
        //Path config_path = Paths.get(out_path, config_name);
        // remove the '\' in the end of path
        String cleaned_path = DirectoryChecker.removeTrailingSlash(out_path);
        String plot_json_path = cleaned_path + "/" + plot_json_name;
        // Define the structure of the JSON data using Maps
        Map<String, Object> root = new LinkedHashMap<>();
        // versionNum object
        Map<String, String> versionNum = new LinkedHashMap<>();
        versionNum.put("version", "1.0");
        root.put("versionNum", versionNum);

        // theme_paras object
        Map<String, Object> themeParas = new LinkedHashMap<>();
        themeParas.put("border_size", 2);
        themeParas.put("border_color", "black");
        themeParas.put("axis_text_x_size", 10);
        themeParas.put("axis_text_x_color", "black");
        themeParas.put("axis_text_y_size", 10);
        themeParas.put("axis_text_y_color", "black");
        themeParas.put("title_size", 11);
        themeParas.put("title_color", "black");
        themeParas.put("title_hjust", 0.5);
        themeParas.put("legend_text_size", 10);
        themeParas.put("legend_text_color", "black");
        root.put("theme_paras", themeParas);

        // avgCov_paras object
        Map<String, Object> avgCovParas = new LinkedHashMap<>();
        avgCovParas.put("line_size", 1.2);
        avgCovParas.put("dash_size", 0.8);
        avgCovParas.put("dash_color", "black");
        root.put("avgCov_paras", avgCovParas);

        // Use Jackson ObjectMapper to write the Map to a JSON file
        ObjectMapper objectMapper = new ObjectMapper();
        // Enable pretty printing for better readability of the output file
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        // Define the output file name
        File outputFile = new File(plot_json_path);

        try {
            // Write the JSON data to the file
            objectMapper.writeValue(outputFile, root);
            System.out.println("Successfully generated " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            // Handle potential IO errors
            System.err.println("Error generating JSON file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
