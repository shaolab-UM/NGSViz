import configSet.InputParameterAttributes;
import configSet.ProcessInputParameters;
import coverageCalculator.LargeIntervalMain;
import coverageCalculator.MainCalculator;
import sqldbOperate.LargeChIPProcessRecordGenomeCoordinateDB;
import sqldbOperate.QueryWholeRegionCoordinate;
import sqldbOperate.exonMode.ExonModelData;

/**
 * @author Benchen Ye
 * @create 2024-11--21:36
 */
public class NGSplot2Main {
    public static void main(String[] args) {
        args = new String[]{
                "-G", "hg19",
                "-R", "genebody",
                //"-C", "/Users/bencheye/myProj/ngsPlot/Data/hesc.H3k4me3.1M.bam",
                "-C", "/Users/bencheye/myProj/ngsPlot/Data/hesc.RNAseq.1M.bam",
                //"-C", "/Users/bencheye/myProj/ngsPlot/Data/input_para.txt",
                "-O", "/Users/bencheye/myProj/ngsPlot/Output/hesc.RNAseq.1M",
                //"-F", "protein_coding, test",
                "-A", "rnaseq",
                "-L", "2000"
        };
        ProcessInputParameters.processInputParameter(args);
        // exonMode for RNAseq
        if(InputParameterAttributes.analysis_type.equals("rnaseq")){
            ExonModelData.getExonModelData(InputParameterAttributes.genome,
                    InputParameterAttributes.DB_tpye,
                    InputParameterAttributes.species);
        }
        MainCalculator.mainCalculator();
        int region_num = QueryWholeRegionCoordinate.region_num;
        System.out.println("region num: " + region_num);
    }

}
