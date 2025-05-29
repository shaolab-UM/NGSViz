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
        args = new String[]{
                "-G", "hg38",
                "-R", "tss",
                "-CP", "/Users/benche/myProj/ngsPlot/NGSViz/NGSViz_setting.json",
                //"-T", "GeneName",
                //"-E", "/Users/bencheye/myProj/ngsPlot/Data/genelist.txt",
                //"-R", "tss",
                "-P", "1",
                //"-R", "tes",
                "-I", "/Users/benche/myProj/ngsPlot/Data/hesc.H3k27me3.1M.bam",
                //"-I", "/Users/bencheye/myProj/ngsPlot/NGSViz/sample_info.csv",
                //"-C", "/Users/bencheye/myProj/ngsPlot/Data/hesc.RNAseq.1M.bam",
                //"-C", "/Users/bencheye/myProj/ngsPlot/Data/input_para.txt",
                "-O", "/Users/benche/myProj/ngsPlot/Output/Test",
                //"-F", "protein_coding",
                //"-A", "transcript",
                //"-M", "bin",
                //"-F", "2000"
        };
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
