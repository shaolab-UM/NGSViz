package configSet;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author Benchen Ye
 * @create 2024-11--19:08
 * @function generate the config file
 */
public class GenerateJsonConfig extends InputParameterAttributes {
    public static void main(String[] args) {
        String config_name = "/Users/bencheye/myProj/ngsPlot/Output/config_test.json";
        // read in the parameter
        args = new String[]{
                "-G", "hg19",
                "-R", "genebody",
                "-C", "/Users/bencheye/myProj/ngsPlot/Data/hesc.RNAseq.1M.bam",
                "-O", "/Users/bencheye/myProj/ngsPlot/Output/hesc.RNAseq.1M",
                "-SS", "same"
        };
        //GetCommandParameter.getInputParameter(args);
        // get the value of parameter
        String genome = GetCommandParameter.getGenome();
        String region_plot = GetCommandParameter.getRegion2Plot();
        // check bam/txt
        String input_file = GetCommandParameter.getInputFile();
        String output_path = GetCommandParameter.getOutputPath();

        // create JSONObject to save config
        JSONObject config = new JSONObject();
        // create "mandatory_parameters" JSONObject
        JSONObject mandatoryParameters = new JSONObject();
        mandatoryParameters.put("bam_file", "/Users/bencheye/myProj/ngsPlot/Data/hesc.RNAseq.1M.bam");
        mandatoryParameters.put("genome", "hg19");
        mandatoryParameters.put("output_name", "/Users/bencheye/myProj/ngsPlot/Data/hesc.RNAseq.1M_output.txt");
        mandatoryParameters.put("region_plot", "genebody");
        config.put("mandatory_parameters", mandatoryParameters);
        // create "common_parameters" JSONObject
        JSONObject commonParameters = new JSONObject();
        commonParameters.put("num_datapoints", 100);
        commonParameters.put("middle_datapoints", 61);
        commonParameters.put("flanking_region_datapoints", 20);
        // create JSONArray to save list data
        JSONArray plotTitle_array = new JSONArray();
        plotTitle_array.put("low");
        plotTitle_array.put("high");
        commonParameters.put("plot_title", plotTitle_array);
        // put "common_parameters" JSONObject to the config
        config.put("common_parameters", commonParameters);
        try {
            // identify config file path and name
            FileWriter config_file = new FileWriter(config_name);
            // transform the JSONObject into character and write in file
            // // Indentation is four spaces.
            config_file.write(config.toString(4));
            config_file.close();
            System.out.println("JSON config file has been generated!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
