package configSet;

/**
 * @author Benchen Ye
 * @create 2024-09--15:19
 */

import com.fasterxml.jackson.annotation.JsonProperty;

public class ReadConfig {
    @JsonProperty("mandatory_parameters")
    private MandatoryParameters mandatory_parameters;
    @JsonProperty("common_parameters")
    private CommonParameters common_parameters;
    @JsonProperty("coverage_calculator")
    private CalculatorParameters calculator_parameters;

    // Getters
    public MandatoryParameters getMandatoryParameters() {
        return mandatory_parameters;
    }
    public CommonParameters getCommonParameters() {
        return common_parameters;
    }
    public CalculatorParameters getCalculatorParaters() {
        return calculator_parameters;
    }

    public static class MandatoryParameters {
        @JsonProperty("bam_file")
        private String bam_file;
        @JsonProperty("genome")
        private String genome;
        @JsonProperty("output_name")
        private String output_name;
        @JsonProperty("region_plot")
        private String region_plot;

        // Getters
        public String getBamFile() {
            return bam_file;
        }
        public String getGenome() {
            return genome;
        }
        public String getOutputName() {
            return output_name;
        }
        public String getRegionPlot() {
            return region_plot;
        }
    }

    public static class CommonParameters {
        @JsonProperty("num_datapoints")
        private int num_data_points;
        @JsonProperty("middle_datapoints")
        private int middle_datapoints;
        @JsonProperty("flanking_region_datapoints")
        private int flanking_region_datapoints;
        @JsonProperty("plot_title")
        private String[] plot_title;

        // Getters
        public int getNumDatapoints() {
            return num_data_points;
        }
        public int getMiddleDatapoints() {
            return middle_datapoints;
        }
        public int getFlankingRegionDatapoints() {
            return flanking_region_datapoints;
        }
        public String[] getPlotTitle() {
            return plot_title;
        }
    }

    public static class CalculatorParameters {
        @JsonProperty("database")
        private String database;

        @JsonProperty("flanking_size")
        private int flanking_size;

        @JsonProperty("flank_factor")
        private int flank_factor;
        @JsonProperty("robust")
        private int robust;
        @JsonProperty("samprate")
        private int samprate;
        @JsonProperty("cores_num")
        private int cores_num;

        @JsonProperty("scaler_method")
        private String scaler_method;
        @JsonProperty("min_mapq")
        private int min_mapq;
        @JsonProperty("frag_len")
        private int frag_len;
        @JsonProperty("buf_size")
        private int buf_size;

        @JsonProperty("strand_spec")
        private String strand_spec;
        @JsonProperty("fi_tag")
        private int fi_tag;
        @JsonProperty("gene_list")
        private String[] gene_list;
        @JsonProperty("rnaseq_gb")
        private boolean rnaseq_gb;
        @JsonProperty("large_interval")
        private int large_interval;
        @JsonProperty("interval_type")
        private String interval_type;
        @JsonProperty("region_labels")
        private String[] region_labels;


        // Getter
        public String getDatabase() {
            return database;
        }

        public int getFlankingSize() {
            return flanking_size;
        }

        public int getFlankFactor() {
            return flank_factor;
        }
        public int getRobust() {
            return robust;
        }
        public int getSamprate() {
            return samprate;
        }
        public int getCoresNum() {
            return cores_num;
        }


        public String getScalerMethod() {
            return scaler_method;
        }
        public int getMinMapq() {
            return min_mapq;
        }
        public int getFragLen() {
            return frag_len;
        }
        public int getBufSize() {
            return buf_size;
        }

        public String getStrandSpec() {
            return strand_spec;
        }
        public int getFiTag() {
            return fi_tag;
        }
        public String[] getGeneList() {
            return gene_list;
        }
        public boolean getRnaseqGb() {
            return rnaseq_gb;
        }
        public int getLargeInterval() {
            return large_interval;
        }
        public String getIntervalType() {
            return interval_type;
        }
        public String[] getRegionLabels() {
            return region_labels;
        }
    }
}

