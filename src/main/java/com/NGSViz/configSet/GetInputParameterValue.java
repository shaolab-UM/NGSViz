package com.NGSViz.configSet;

import java.util.List;

/**
 * @author Benchen Ye
 * @ create 2024-11--21:10
 */
public class GetInputParameterValue extends InputParameterAttributes {
    // Parse by traversing the command line argument array.
    public static void getInputParametersValue (String[] args) {
        for (int i = 0; i < args.length; i = i + 2) {
            // first parameter
            input_db_path = setParameterValue(input_db_path, "-DB", args, i);
            // necessary parameters
            genome = setParameterValue(genome, "-G", args, i);
            region_type = setParameterValue(region_type, "-R", args, i);
            input_file = setParameterValue(input_file, "-I", args, i);
            output_path = setParameterValue(output_path, "-O", args, i);
            // Option parameters
            // system config path
            sys_config_path = setParameterValue(sys_config_path,"-CP", args, i);
            // Database
            DB_type = setParameterValue(DB_type,"-D", args, i);
            // gene list
            genes = setParameterValue(genes,"-X", args, i);
            // analysis title
            title = setParameterValue(title,"-T", args, i);
            // analysis type
            analysis_type = setParameterValue(analysis_type,"-A", args, i);
            // subset for region - B
            biotype = setParameterValue(biotype,"-B", args, i);
            // Flanking region size
            flank_region = setParameterValue(flank_region,"-F", args, i);
            // Flanking size factor
            flank_factor = setParameterValue(flank_factor,"-N", args, i);
            // Robust statistics
            //robust = setParameterValue(robust,"-RB", args, i);
            // Random sampling rate
            //random_sampling_rate = setParameterValue(random_sampling_rate,"-S", args, i);
            // scale_ratio, such as spike-in calibriation
            scale_ratio = setParameterValue(scale_ratio,"-S", args, i);
            // bed db path
            bedDB_path = setParameterValue(bedDB_path, "-BD", args, i);
            // number of datapoint
            num_datapoints = setParameterValue(num_datapoints,"-DP", args, i);
            // Set cores number
            core_num = setParameterValue(core_num,"-P", args, i);
            // Algorithm for coverage vector normalization (scale data)
            scaler_method = setParameterValue(scaler_method,"-M", args, i);
            // Gene chunk size
            chunk_size = setParameterValue(chunk_size,"-CZ", args, i);
            // Mapping quality cutoff
            min_mapq = setParameterValue(min_mapq,"-MQ", args, i);
            // Fragment length
            frag_len = setParameterValue(frag_len,"-FL", args, i);
            // Strand-specific coverage
            strand_spec = setParameterValue(strand_spec,"-SS", args, i);
            // Image output forbidden tag
            //fi_tag = setParameterValue(fi_tag,"-FI", args, i);
        }
    }

    //
    public static String setParameterValue(String var_name, String parameter, String [] args, int index){
        String res_out = getParameterValue(parameter, args, index);
        if (res_out != null) {var_name = res_out;}
        return var_name;
    }
    public static List<String> setSpecialParameterValue(List<String> var_name, String parameter, String [] args, int index){
        String res_out = getParameterValue(parameter, args, index);
        if (res_out != null){
            var_name.add(res_out);
        }
        return var_name;
    }
    public static int setParameterValue(int var_name, String parameter, String [] args, int index){
        String res_out = getParameterValue(parameter, args, index);
        if (res_out != null) {
            int converted_number = Integer.parseInt(res_out);
            var_name = converted_number;
        }
        return var_name;
    }
    public static double setParameterValue(double var_name, String parameter, String [] args, int index){
        String res_out = getParameterValue(parameter, args, index);
        if (res_out != null) {
            double converted_number = Double.parseDouble(res_out);
            var_name = converted_number;
        }
        return var_name;
    }
    public static List<String> setParameterValue(List<String> var_name, String parameter, String [] args, int index){
        String res_out = getParameterValue(parameter, args, index);
        if (res_out != null){
            String [] str_res = res_out.split(",");
            for (String item : str_res) {
                var_name.add(item);
            }
        }
        return var_name;
    }
    // get the value of input parameters
    public static String getParameterValue(String parameter, String [] args, int index) {
        String out_value = null;
        if (parameter.equals(args[index])) {
            checkParameterValue(parameter, args[index + 1]);
            out_value = args[index + 1];
            System.out.println(parameter + ": " + out_value);
        }
        return out_value;
    }
    public static void checkParameterValue(String parameter, String char_str) {
        if (char_str != null && char_str.length() > 0) {
            // get first character
            char firstChar = char_str.charAt(0);
            // check whether it is '-'
            if (firstChar == '-') {
                System.out.println("Parameter `" + parameter + "` without value!");
                System.exit(-1);
            }
        } else {
            System.out.println("The value of parameter " + parameter + " is empty!");
            System.exit(-1);
        }
    }
}
