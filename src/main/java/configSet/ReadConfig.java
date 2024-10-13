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
        // Getters
        public int getNumDatapoints() {
            return num_data_points;
        }
    }

    public static class CalculatorParameters {
        @JsonProperty("scaler_method")
        private String scaler_method;
        @JsonProperty("flanking_size")
        private int flanking_size;
        @JsonProperty("buf_size")
        private int buf_size;
        @JsonProperty("min_mapq")
        private int min_mapq;
        @JsonProperty("frag_len")
        private int frag_len;
        // Getter
        public String getScalerMethod() {
            return scaler_method;
        }
        public int getFlankingSize() {
            return flanking_size;
        }
        public int getBufSize() {
            return buf_size;
        }
        public int getMinMapq() {
            return min_mapq;
        }
        public int getFragLen() {
            return frag_len;
        }

    }
}

