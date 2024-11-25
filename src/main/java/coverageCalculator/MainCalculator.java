package coverageCalculator;

import configSet.CommonFinalParas;
import configSet.InputParameterAttributes;
import coverageCalculator.dataScaler.DataScaler;
import htsjdk.samtools.util.Interval;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Benchen Ye
 * @create 2024-11--14:47
 */
public class MainCalculator extends InputParameterAttributes {
    public static String bam_file;
    public static String gene_subset;
    public static String title_subset;
    public static void mainCalculator(){
        for (int i=0; i<bam_list.size(); i++) {
            bam_file = bam_list.get(i);
            System.out.println("The bam file will be analysing: " + bam_file);
            gene_subset = gene_list.get(i);
            System.out.println("The gene subset will be analysing: " + gene_subset);
            title_subset = analysis_title.get(i);
            System.out.println("The title subset will be analysing: " + title_subset);
            // check whether bam file exist
            // split bam file
            System.out.println("Start coverage calculation!");
            System.out.println("---------");
            //LargeIntervalMain.largeIntervalMain(bam_file);
            RNAseqIntervalMain.rnaseqIntervalMain(bam_file);
        }
    }
}
