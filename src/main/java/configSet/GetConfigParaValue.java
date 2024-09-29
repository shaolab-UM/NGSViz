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
    ObjectMapper objectMapper = new ObjectMapper();

    {
        try {
            config = objectMapper.readValue(new File(config_path), ReadConfig.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public int getNumDatapoints() {
        int num_datapoints = config.getCommonParameters().getNumDatapoints();
        return num_datapoints;
    }
    public String getScalerMethod() {
        String scaler_method = config.getCommonParameters().getScalerMethod();
        return scaler_method;
    }
    public int getFlankingSize() {
        int flanking_size = config.getCommonParameters().getFlankingSize();
        return flanking_size;
    }
    public int getBufSize() {
        int buf_size = config.getCommonParameters().getBufSize();
        return buf_size;
    }
    public int getMinMapq() {
        int min_mapq = config.getCommonParameters().getMinMapq();
        return min_mapq;
    }
    public int getFragLen() {
        int frag_len = config.getCommonParameters().getFragLen();
        return frag_len;
    }
}
