package com.NGSViz.configSet;

/**
 * @author Benchen Ye
 * @ create 2024-11--20:34
 */
public class SetDefaultOptionParameter extends InputParameterAttributes {
    // set the default value for option parameters.
    public static void setDefaultOptionParameter() {
        System.out.println("start set the default value for option parameter without a input value!");
        System.out.println("---------");
        if (DB_type == null){
            DB_type = "RefSeq";
            System.out.println("the usage of DB tpye is : " + DB_type);
        }
        if (analysis_type.equals("transcript")) {
            System.out.println("analysis type is : " + analysis_type);
        }
        if (biotype.equals("protein_coding")){
            System.out.println("The biotype is : " + biotype);
        }
        if (flank_region == 0) {
            flank_region = CheckLegal2InputParameter.getFlankRegion(region_type);
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
        if (scaler_method.equals("bin")) {
            System.out.println("scaler_method: " + scaler_method);
        }
        int chunk_size;
        if (BATCH_SIZE == 500){
            System.out.println("chunk_size: " + BATCH_SIZE);
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
        System.out.println("Finish set the default value for option parameter without a input value!");
        System.out.println("---------");
    }
}
