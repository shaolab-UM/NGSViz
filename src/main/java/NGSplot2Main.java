import configSet.GetCommandParameter;

/**
 * @author Benchen Ye
 * @create 2024-11--21:36
 */
public class NGSplot2Main {
    public static void main(String[] args) {
        args = new String[]{
                "-G", "hg19",
                "-R", "genebody",
                "-C", "/Users/bencheye/myProj/ngsPlot/Data/hesc.RNAseq.1M.bam",
                "-O", "/Users/bencheye/myProj/ngsPlot/Output/hesc.RNAseq.1M",
                "-SS", "same"
        };

        //GetCommandParameter.getInputParameter(args);

    }
}
