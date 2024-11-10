import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import configSet.CheckLegal2InputParameter;
import configSet.GetCommandParameter;
import configSet.InputParameterAttributes;
import configSet.ProcessInputParameters;

/**
 * @author Benchen Ye
 * @create 2024-11--11:35
 */
public class test {
    public static void main(String[] args) {
        args = new String[]{
                "-G", "hg19",
                "-R", "genebody",
                "-C", "/Users/bencheye/myProj/ngsPlot/Data/hesc.RNAseq.1M.bam",
                "-O", "/Users/bencheye/myProj/ngsPlot/Output/hesc.RNAseq.1M",
                "-L", "2000"
        };
        //ProcessInputParameters.processInputParameter(args);
        String file_path = "/Users/bencheye/myProj/ngsPlot/Data/input_para.txt";
        //getFileExtension(file_path);
        CheckLegal2InputParameter.CheckFilePath(file_path);
    }


}
