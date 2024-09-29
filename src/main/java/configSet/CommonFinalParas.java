package configSet;

/**
 * @author Benchen Ye
 * @create 2024-09--19:45
 */
public class CommonFinalParas {
    public static final int num_datapoints;
    public static final String scaler_method;
    public static final int flanking_size;
    public static final int buf_size;
    public static final int min_mapq;
    public static final int frag_len;

    static {
        GetConfigParaValue config_obj = new GetConfigParaValue();
        num_datapoints = config_obj.getNumDatapoints();
        scaler_method = config_obj.getScalerMethod();
        flanking_size = config_obj.getFlankingSize();
        buf_size = config_obj.getBufSize();
        min_mapq = config_obj.getMinMapq();
        frag_len = config_obj.getFragLen();
    }
}
