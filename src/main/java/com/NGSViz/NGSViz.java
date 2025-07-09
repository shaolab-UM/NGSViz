package com.NGSViz;

import com.NGSViz.configSet.InputParameterAttributes;
import com.NGSViz.configSet.ProcessInputParameters;
import com.NGSViz.coverageCalculator.MainCalculator;
import com.NGSViz.sqldbOperate.exonMode.ExonModelData;

import java.io.IOException;

/**
 * @author Benchen Ye
 * @create 2024-11--21:36
 */
public class NGSViz {
    public static void main(String[] args) throws IOException {
        args = new String[]{
                "-G", "hg19",
                "-R", "exon",
                "-CP", "/Users/benche/myProj/ngsPlot/NGSViz/NGSViz_setting.json",
                //"-DP", "100",
                //"-DB", "/Users/benche/myProj/ngsPlot/DB/DB/Mus_musculus/NGSViz_mm9_RefSeq.db",
                //"-T", "GeneName",
                //"-E", "/Users/bencheye/myProj/ngsPlot/Data/genelist.txt",
                //"-R", "tss",
                "-P", "8",
                //"-R", "tes",
                //"-I", "/Users/benche/myProj/ngsPlot/NGSViz/Samples/sample_info.csv",
                //"-I", "/Users/benche/myProj/ngsPlot/Data/LX/sgControl_LSD2.bam",
                //"-I", "/Users/benche/myProj/ngsPlot/Data/GSE209153_H3K4me3_sample/GSE209153_H3K4me3_0.5.sorted.bam",
                "-I", "/Users/benche/myProj/ngsPlot/Data/hesc.RNAseq.1M.bam",
                //"-I", "/Users/benche/myProj/ngsPlot/Data/hesc.H3k27me3.1M.bam",
                //"-I", "/Users/benche/myProj/ngsPlot/Data/hesc.H3k4me3.1M.bam",
                //"-I", "/Users/bencheye/myProj/ngsPlot/NGSViz/sample_info.csv",
                //"-C", "/Users/bencheye/myProj/ngsPlot/Data/hesc.RNAseq.1M.bam",
                //"-C", "/Users/bencheye/myProj/ngsPlot/Data/input_para.txt",
                "-O", "/Users/benche/myProj/ngsPlot/Output/Test",
                //"-F", "protein_coding",
                //"-A", "transcript",
                //"-M", "bin",
                //"-F", "2000"
        };
        long startTimeMillis = System.currentTimeMillis();

        ProcessInputParameters.processInputParameter(args);
        // exonMode for exon
        if(InputParameterAttributes.analysis_type.equals("exon")){
            ExonModelData.getExonModelData(InputParameterAttributes.tbl_name,
                    InputParameterAttributes.analysis_type,
                    InputParameterAttributes.biotype);
        }
        MainCalculator.mainCalculator();

        long endTimeMillis = System.currentTimeMillis();
        long durationNano = endTimeMillis - startTimeMillis;
        double durationSeconds = durationNano / 1_000.0;
        System.out.println("----------------------------------------");
        System.out.println("Running time: " + durationSeconds);
        System.out.println("----------------------------------------");
    }

}
