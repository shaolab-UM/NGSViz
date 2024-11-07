import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import configSet.GetCommandParameter;

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
                "-SS", "same"
        };
        GetCommandParameter.getInputParameter(args);
        String getRegion2Plot = GetCommandParameter.getRegion2Plot();
        //System.out.println(getRegion2Plot);
        int flank_size = GetCommandParameter.getFlankRegion();
        //System.out.println(flank_size);
    }

}
