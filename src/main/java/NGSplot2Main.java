import configSet.InputParameterAttributes;
import configSet.ProcessInputParameters;
import coverageCalculator.MainCalculator;
import sqldbOperate.QueryWholeRegionCoordinateCP;
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
                //"-T", "GeneName",
                //"-E", "/Users/bencheye/myProj/ngsPlot/Data/genelist.txt",
                //"-R", "tss",
                "-D", "RefSeq",
                //"-R", "tes",
                "-C", "/Users/bencheye/myProj/ngsPlot/Data/hesc.H3k27me3.1M.bam",
                //"-C", "/Users/bencheye/myProj/ngsPlot/Data/hesc.RNAseq.1M.bam",
                //"-C", "/Users/bencheye/myProj/ngsPlot/Data/input_para.txt",
                "-O", "/Users/bencheye/myProj/ngsPlot/Output/Result",
                //"-F", "protein_coding, test",
                "-A", "transcript",
                "-AL", "bin",
                "-L", "2000"
        };
        ProcessInputParameters.processInputParameter(args);
        // exonMode for exon
        if(InputParameterAttributes.analysis_type.equals("exon")){
            ExonModelData.getExonModelData(InputParameterAttributes.tbl_name,
                    InputParameterAttributes.analysis_type,
                    InputParameterAttributes.biotype);
        }
        MainCalculator.mainCalculator();
        int region_num = QueryWholeRegionCoordinateCP.region_num;
        System.out.println("region num: " + region_num);
    }

}
