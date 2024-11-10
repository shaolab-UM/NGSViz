package configSet;

/**
 * @author Benchen Ye
 * @create 2024-11--20:05
 * @function get the value of each input parameter and put the value into vars
 */
public class ProcessInputParameters extends InputParameterAttributes {
    public static void processInputParameter(String[] args) {
        if (args.length % 2 ==1 || args.length == 0){
            System.out.println("Unpaired argument and value.");
            System.exit(-1);
        }
        // Parse by traversing the command line argument array.
        for (int i = 0; i < args.length; i = i + 2) {
            String res_out = null;
            // necessary parameters
            genome = setParameterValue(genome, "-G", args, i);
            region_plot = setParameterValue(region_plot, "-R", args, i);
            input_file = setParameterValue(input_file, "-C", args, i);
            output_path = setParameterValue(output_path, "-O", args, i);
            // Option parameters
            // Database
            DB_tpye = setParameterValue(DB_tpye,"-D", args, i);
            // analysis title
            analysis_title = setParameterValue(analysis_title,"-T", args, i);
            // gene list
            gene_list = setParameterValue(gene_list,"-E", args, i);
            // analysis type
            analysis_type = setParameterValue(analysis_type,"-F", args, i);
            // Flanking region size
            flank_region = setParameterValue(flank_region,"-L", args, i);
            // Flanking size factor
            flank_factor = setParameterValue(flank_factor,"-N", args, i);
            // Robust statistics
            robust = setParameterValue(robust,"-RB", args, i);
            // Random sampling rate
            random_sampling_rate = setParameterValue(random_sampling_rate,"-S", args, i);
            // Set cores number
            core_num = setParameterValue(core_num,"-P", args, i);
            // Algorithm for coverage vector normalization (scale data)
            scaler_method = setParameterValue(scaler_method,"-AL", args, i);
            // Gene chunk size
            chunk_size = setParameterValue(chunk_size,"-CS", args, i);
            // Mapping quality cutoff
            min_mapq = setParameterValue(min_mapq,"-MQ", args, i);
            // Fragment length
            frag_len = setParameterValue(frag_len,"-FL", args, i);
            // Strand-specific coverage
            strand_spec = setParameterValue(strand_spec,"-SS", args, i);
            // Image output forbidden tag
            fi_tag = setParameterValue(fi_tag,"-FI", args, i);
        }
        // set and print the default value for option parameters
        setDefaulatOptionParameter();
        // check whether is legal value for input paramters
        CheckLegal2InputParameter.checkLegal2InputParameter();
    }
    // set the default value for option parameters.
    public static void setDefaulatOptionParameter() {
        if (analysis_type.equals("chipseq")) {
            System.out.println("analysis type is : " + analysis_type);
        }
        if (flank_region == 0) {
            flank_region = CheckLegal2InputParameter.getFlankRegion(region_plot);
            System.out.println("flank_region: " + flank_region);
        }
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
        if (scaler_method.equals("spline")) {
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
        if (strand_spec.equals("both")) {
            System.out.println("strand_spec: " + strand_spec);
        }
        if (fi_tag == 0){
            System.out.println("fi_tag: " + fi_tag);
        }
    }
    //
    public static String setParameterValue(String var_name, String parameter, String [] args, int index){
        String res_out = getParameterValue(parameter, args, index);
        if (res_out != null) {var_name = res_out;}
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
