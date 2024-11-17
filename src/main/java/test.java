import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import configSet.CheckLegal2InputParameter;
import configSet.GetCommandParameter;
import configSet.InputParameterAttributes;
import configSet.ProcessInputParameters;
import sqldbOperate.QueryDefaultDB;
import utils.DirectoryChecker;

/**
 * @author Benchen Ye
 * @create 2024-11--11:35
 */
public class test extends InputParameterAttributes {
    public static void main(String[] args) {
        args = new String[]{
                "-G", "hg19",
                "-R", "genebody",
                "-C", "/Users/bencheye/myProj/ngsPlot/Data/hesc.RNAseq.1M.bam",
                //"-C", "/Users/bencheye/myProj/ngsPlot/Data/input_para.txt",
                "-O", "/Users/bencheye/myProj/ngsPlot/Output/hesc.RNAseq.1M",
                //"-F", "protein_coding, test",
                "-A", "rnaseq",
                "-L", "2000"
        };
        ProcessInputParameters.processInputParameter(args);
        String file_path = "/Users/bencheye/myProj/ngsPlot/Data/input_para.txt";
        //getFileExtension(file_path);
        //CheckLegal2InputParameter.CheckFilePath(file_path);
        //String query_key = "PointLab";





    }


}
