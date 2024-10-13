package configSet;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

/**
 * @author Benchen Ye
 * @create 2024-09--16:48
 */

public class GetConfigParaValue {
    // config file
    private String config_path = "src/main/resources/config.json";
    private ReadConfig config;
    ObjectMapper object_mapper = new ObjectMapper();

    {
        try {
            config = object_mapper.readValue(new File(config_path), ReadConfig.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    // MandatoryParameters
    public String getBamFile() {
        String bam_file = config.getMandatoryParameters().getBamFile();
        return bam_file;
    }
    public String getGenome() {
        String genome = config.getMandatoryParameters().getGenome();
        return genome;
    }
    public String getOutputName() {
        String output_name = config.getMandatoryParameters().getOutputName();
        return output_name;
    }
    public String getRegionPlot() {
        String output_name = config.getMandatoryParameters().getRegionPlot();
        return output_name;
    }

    // CommonParameters
    public int getNumDatapoints() {
        int num_datapoints = config.getCommonParameters().getNumDatapoints();
        return num_datapoints;
    }
    public int getMiddleDatapoints(){
        int num_datapoints = config.getCommonParameters().getMiddleDatapoints();
        return num_datapoints;
    }
    public int getFlankingRegionDatapoints(){
        int flanking_region_datapoints = config.getCommonParameters().getFlankingRegionDatapoints();
        return flanking_region_datapoints;
    }
    public String[] getPlotTitle(){
        String[] plot_title = config.getCommonParameters().getPlotTitle();
        return plot_title;
    }

    // CalculatorParaters
    public String getDatabase(){
        String database = config.getCalculatorParaters().getDatabase();
        return database;
    }

    public int getFlankingSize() {
        int flanking_size = config.getCalculatorParaters().getFlankingSize();
        return flanking_size;
    }

    public int getFlankFactor() {
        int flank_factor = config.getCalculatorParaters().getFlankFactor();
        return flank_factor;
    }
    public int getRobust() {
        int robust = config.getCalculatorParaters().getRobust();
        return robust;
    }
    public int getSamprate() {
        int samprate = config.getCalculatorParaters().getSamprate();
        return samprate;
    }
    public int getCoresNum() {
        int cores_num = config.getCalculatorParaters().getCoresNum();
        return cores_num;
    }

    public String getScalerMethod() {
        String scaler_method = config.getCalculatorParaters().getScalerMethod();
        return scaler_method;
    }
    public int getMinMapq() {
        int min_mapq = config.getCalculatorParaters().getMinMapq();
        return min_mapq;
    }
    public int getFragLen() {
        int frag_len = config.getCalculatorParaters().getFragLen();
        return frag_len;
    }
    public int getBufSize() {
        int buf_size = config.getCalculatorParaters().getBufSize();
        return buf_size;
    }

    public String getStrandSpec() {
        String strand_spec = config.getCalculatorParaters().getStrandSpec();
        return strand_spec;
    }
    public int getFiTag() {
        int fi_tag = config.getCalculatorParaters().getFiTag();
        return fi_tag;
    }
    public String[] getGeneList() {
        String[] gene_list = config.getCalculatorParaters().getGeneList();
        return gene_list;
    }
    public boolean getRnaseqGb() {
        boolean rnaseq_gb = config.getCalculatorParaters().getRnaseqGb();
        return rnaseq_gb;
    }
    public int getLargeInterval() {
        int large_interval = config.getCalculatorParaters().getLargeInterval();
        return large_interval;
    }
    public String getIntervalType() {
        String interval_type = config.getCalculatorParaters().getIntervalType();
        return interval_type;
    }
    public String[] getRegionLabels() {
        String[] region_labels = config.getCalculatorParaters().getRegionLabels();
        return region_labels;
    }

}
