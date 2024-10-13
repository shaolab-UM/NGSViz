import com.fasterxml.jackson.databind.ObjectMapper;
import configSet.CommonFinalParas;
import configSet.GetConfigParaValue;

import java.io.File;
import java.io.IOException;

/**
 * @author Benchen Ye
 * @create 2024-09--16:08
 */

public class Main {
    public static void main(String[] args) {
        //GetConfigParaValue config_obj = new GetConfigParaValue();
        System.out.println(CommonFinalParas.middle_datapoints);
        System.out.println(CommonFinalParas.flanking_region_datapoints);
        System.out.println(CommonFinalParas.plot_title[0]);
        System.out.println(CommonFinalParas.database);
        System.out.println(CommonFinalParas.flank_factor);
        System.out.println(CommonFinalParas.robust);
        System.out.println(CommonFinalParas.samprate);
        System.out.println(CommonFinalParas.cores_num);

        System.out.println(CommonFinalParas.strand_spec);
        System.out.println(CommonFinalParas.fi_tag);
        System.out.println(CommonFinalParas.gene_list[0]);
        System.out.println(CommonFinalParas.large_interval);
        System.out.println(CommonFinalParas.interval_type);
        System.out.println(CommonFinalParas.region_labels[1]);
    }
}




