package coverageCalculator;

import ReadBam.ReadBam;
import configSet.InputParameterAttributes;
import htsjdk.samtools.SamReader;
import utils.CalculateLog2Matrix;
import utils.Matrix2CSV;
import ReadBam.BamFileLibrarySize;


/**
 * @author Benchen Ye
 * @create 2024-11--14:47
 */
public class MainCalculator extends InputParameterAttributes {
    private static String bam_file;
    private static String gene_subset;
    private static String title_subset;
    private static double trim_ratio = InputParameterAttributes.robust;
    private static String heatmap_data_path = "/Users/bencheye/myProj/ngsPlot/Output/coverage_matrix_heatmap.csv";
    private static String average_coverage_path = "/Users/bencheye/myProj/ngsPlot/Output/average_coverage_matrix.csv";
    private static boolean paired_mode;
    private static String paired_bam = null;
    public static void mainCalculator(){
        for (int i=0; i<bam_list.size(); i++) {
            String[] bam_file_list = bam_list.get(i).split(":", 2);
            bam_file = bam_file_list[0];
            // Is the paired mode available?
            if (bam_file_list.length == 2) {
                paired_mode = true;
                paired_bam = bam_file_list[1];
                System.out.println("paired pattern is available, the pair-end bam file is : " + bam_file_list[1]);
            }
            System.out.println("The bam file will be analysing: " + bam_file);
            gene_subset = gene_list.get(i);
            System.out.println("The gene subset will be analysing: " + gene_subset);
            title_subset = analysis_title.get(i);
            System.out.println("The title subset will be analysing: " + title_subset);
            // check whether bam file exist
            // split bam file
            System.out.println("Start coverage calculation!");
            System.out.println("---------");
            // coverage_scaled_matrix is data matrix for heatmap
            double[][] coverage_scaled_matrix = EachQueryRegionProcess.processEachQueryRegion(bam_file);
            //RNAseqIntervalMain.rnaseqIntervalMain(bam_file);

            if(paired_mode){
                // calculate background
                System.out.println("Paired mode: to calculate background.");
                double[][] bkg_coverage_scaled_matrix = EachQueryRegionProcess.processEachQueryRegion(paired_bam);
                // libsize
                // get the library size of bam file
                SamReader bam_reader = ReadBam.getBamReader(bam_file);
                double library_size = BamFileLibrarySize.getLibrarySize(bam_reader);
                double result_pseudo_rpm = (1e6 / library_size);
                // get the library size of paired bam file
                SamReader paired_bam_reader = ReadBam.getBamReader(paired_bam);
                double paired_library_size = BamFileLibrarySize.getLibrarySize(bam_reader);
                double bkg_pseudo_rpm = (1e6 / paired_library_size);
                coverage_scaled_matrix = CalculateLog2Matrix.calculateLog2Matrix(coverage_scaled_matrix,
                        bkg_coverage_scaled_matrix, result_pseudo_rpm, bkg_pseudo_rpm);
            }

            // Calculate standard errors (SEM) for matrix if needed. Shut off SEM in single gene case.
            double[] confi_matrix = AverageCoverage.calculateAverageCoverage(coverage_scaled_matrix, trim_ratio);

            // output the result
            System.out.println("Coverage Scaled Matrix finish!");
            System.out.println("The number of row is: " + coverage_scaled_matrix.length);
            System.out.println("The number of column is: " + coverage_scaled_matrix[0].length);

            // Return avg. profile.
            double[] average_coverage_matrix = AverageCoverage.calculateAverageCoverage(coverage_scaled_matrix, trim_ratio);
            // save the data
            // Book-keep this matrix for heatmap.

            //Matrix2CSV.saveMatrix2CSV(coverage_scaled_matrix, row_names, heatmap_data_path);
            Matrix2CSV.saveMatrix2CSV(average_coverage_matrix, average_coverage_path);
        }
    }
}
