package configSet;

/**
 * @author Benchen Ye
 * @create 2024-09--15:19
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;



public class ReadConfig {
    @JsonProperty("common_parameters")
    private CommonParameters commonParameters;
    @JsonProperty("server")
    private Server server;

    // Getters
    public CommonParameters getCommonParameters() {
        return commonParameters;
    }
    public Server getServer() {
        return server;
    }

    public static class CommonParameters {
        @JsonProperty("num_datapoints")
        private int numDatapoints;
        @JsonProperty("scaler_method")
        private String scalerMethod;
        @JsonProperty("flanking_size")
        private int flankingSize;
        @JsonProperty("buf_size")
        private int bufSize;
        @JsonProperty("min_mapq")
        private int minMapq;
        @JsonProperty("frag_len")
        private int fragLen;

        // Getters
        public int getNumDatapoints() {
            return numDatapoints;
        }
        public String getScalerMethod() {
            return scalerMethod;
        }
        public int getFlankingSize() {
            return flankingSize;
        }
        public int getBufSize() {
            return bufSize;
        }
        public int getMinMapq() {
            return minMapq;
        }
        public int getFragLen() {
            return fragLen;
        }
    }

    public static class Server {
        @JsonProperty("port")
        private int port;
        // Getter
        public int getPort() {
            return port;
        }
    }
}

