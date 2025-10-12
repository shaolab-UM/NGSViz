package com.NGSViz;

import com.NGSViz.configSet.InputParameterAttributes;
import com.NGSViz.configSet.ProcessInputParameters;
import com.NGSViz.coverageCalculator.MainCalculator;
import com.NGSViz.sqldbOperate.exonMode.ExonModelData;
import java.io.IOException;
import java.net.URISyntaxException;

import static com.NGSViz.configSet.InputParameterAttributes.*;

/**
 * @author Benchen Ye
 * @create 2025-07-12:44
 * @function ngsViz
 */
public class NGSViz {
    public static void main(String[] args) throws IOException, URISyntaxException {
        args = new String[]{
                "-G", "hg38",
                "-R", "tss",
                //"-CP", "/Users/benche/myProj/ngsPlot/NGSViz/NGSViz_setting.json",
                "-T", "lung",
                "-I", "/Users/benche/myProj/ngsPlot/Data/featurePlot/feature_fig3D/lung-H3K4me3-ENCFF822ITP.bam",
                "-O", "/Users/benche/myProj/ngsPlot/Result/feature/20251010_Result/lung-H3K4me3-ENCFF822ITP",
                "-S", "1",
                "-CP", "-",
                "-NF", "F",
                //"-BS", "1000",
                "-P", "8",
                //"-FL", "150",
                //"-DB", "/Users/benche/myProj/ngsPlot/DB/DB/Mus_musculus/NGSViz_mm39_RefSeq.db",
               // "-BD", "/Users/benche/myProj/ngsPlot/hg38_ngsViz.bed",
                // "-BD", "/Users/benche/myProj/ngsPlot/hg38_ngsViz.bed",
                //"-T", "GeneName",
                //"-E", "/Users/bencheye/myProj/ngsPlot/Data/genelist.txt",
                //"-R", "tss",
                //"-R", "tes",
                //"-I", "/Users/benche/myProj/ngsPlot/Data/hesc.H3k4me3.1M.bam",
                //"-I", "/Users/benche/myProj/ngsPlot/Data/GSE209153_H3K4me3_sample/GSE209153_H3K4me3_0.1.sorted.bam",
                //"-I", "/Users/benche/myProj/ngsPlot/Data/hesc.H3k27me3.1M.bam",

                //"-I", "/Users/benche/myProj/ngsPlot/Test/AGSsgTFDP1-3-2.bam",
                //"-I", "/Users/benche/myProj/ngsPlot/Data/hesc.RNAseq.1M.bam",
                //"-C", "/Users/bencheye/myProj/ngsPlot/Data/input_para.txt",
                //"-O", "/Users/benche/myProj/ngsPlot/Output/NewTest",

                //"-F", "protein_coding",
                //"-M", "bin",
                //"-F", "2000"
        };
        long startTimeMillis = System.currentTimeMillis();

        ProcessInputParameters.processInputParameter(args);
        // exonMode for exon
    /*
    Here, ExonModelData is used to obtain the lengths, starting sites, and other information of all different transcripts.
    * */
        if (InputParameterAttributes.interval_type.equals("exon")) {
            ExonModelData.getExonModelData(InputParameterAttributes.tbl_name,
                    InputParameterAttributes.interval_type,
                    InputParameterAttributes.biotype);
        }
        MainCalculator.mainCalculator();

        long endTimeMillis = System.currentTimeMillis();
        long durationNano = endTimeMillis - startTimeMillis;
        double durationSeconds = durationNano / 1_000.0;
        System.out.println("----------------------------------------");
        System.out.println("ngsViz analysis has completed!");
        System.out.println("The result is saved in: " + output_path);
        System.out.println("Running time: " + durationSeconds);
        System.out.println("----------------------------------------");
    }
}
