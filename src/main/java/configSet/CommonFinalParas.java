package configSet;

/**
 * @author Benchen Ye
 * @create 2024-09--19:45
 */
public class CommonFinalParas {
    // MandatoryParameters
    public static final String bam_file;
    public static final String genome;
    public static final String output_name;
    public static final String region_plot;

    // CommonParameters
    public static final int num_datapoints;

    // CalculatorParaters
    public static final String scaler_method;
    public static final int flanking_size;
    public static final int buf_size;
    public static final int min_mapq;
    public static final int frag_len;


    static {
        GetConfigParaValue config_obj = new GetConfigParaValue();
        // MandatoryParameters
        bam_file = config_obj.getBamFile();
        genome = config_obj.getGenome();
        output_name = config_obj.getOutputName();
        region_plot = config_obj.getRegionPlot();

        // CommonParameters
        num_datapoints = config_obj.getNumDatapoints();

        // CalculatorParaters
        scaler_method = config_obj.getScalerMethod();
        flanking_size = config_obj.getFlankingSize();
        buf_size = config_obj.getBufSize();
        min_mapq = config_obj.getMinMapq();
        frag_len = config_obj.getFragLen();
    }
}
