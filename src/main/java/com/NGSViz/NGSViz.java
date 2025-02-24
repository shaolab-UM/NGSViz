package com.NGSViz;

import com.NGSViz.configSet.InputParameterAttributes;
import com.NGSViz.configSet.ProcessInputParameters;
import com.NGSViz.coverageCalculator.MainCalculator;
import com.NGSViz.sqldbOperate.exonMode.ExonModelData;

/**
 * @author Benchen Ye
 * @create 2024-11--21:36
 */
public class NGSViz {
    public static void main(String[] args) {
        /*args = new String[]{
                "-G", "hg19",
                "-R", "genebody",
                //"-T", "GeneName",
                //"-E", "/Users/bencheye/myProj/ngsPlot/Data/genelist.txt",
                //"-R", "tss",
                "-D", "RefSeq",
                //"-R", "tes",
                "-I", "/Users/bencheye/myProj/ngsPlot/Data/hesc.H3k27me3.1M.bam",
                //"-C", "/Users/bencheye/myProj/ngsPlot/Data/hesc.RNAseq.1M.bam",
                //"-C", "/Users/bencheye/myProj/ngsPlot/Data/input_para.txt",
                "-O", "/Users/bencheye/myProj/ngsPlot/Output/Result",
                //"-F", "protein_coding",
                "-A", "transcript",
                //"-M", "bin",
                //"-F", "2000"
        };*/

        ProcessInputParameters.processInputParameter(args);
        // exonMode for exon
        if(InputParameterAttributes.analysis_type.equals("exon")){
            ExonModelData.getExonModelData(InputParameterAttributes.tbl_name,
                    InputParameterAttributes.analysis_type,
                    InputParameterAttributes.biotype);
        }
        MainCalculator.mainCalculator();
    }

}
