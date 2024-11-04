package sqldbOperate;

import DataPrepare.DataPointNum;
import ReadBam.QueryGenomeRange;
import ReadBam.ReadBam;

import configSet.CommonFinalParas;
import coverageCalculator.PhysicalCoverageCalculator;
import coverageCalculator.TrimBuffer;
import coverageCalculator.dataScaler.DataScaler;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.util.Interval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Benchen Ye
 * @create 2024-10--16:16
 * @function calculate the scaled coverage of large ChIP for each query region (record)
 */
public class LargeChIPProcessRecordGenomeCoordinateDB {
        private static String region_plot = CommonFinalParas.region_plot;
        private static String bam_file_path = CommonFinalParas.bam_file;
        private static int flanking_size = CommonFinalParas.flanking_size;
        private static int buf_size = CommonFinalParas.buf_size;
        private static String scaler_method = CommonFinalParas.scaler_method;
        private static int flank_factor = CommonFinalParas.flank_factor;
        private static int flank_size;

        public static List<Double> processRecord(Map<String, Object> record) {
            System.out.println("Processing record: " + record);
            // genome coordinate information
            String chr_name = (String) record.get("chrom");
            int start_pos = (int) record.get("start");
            int end_pos = (int) record.get("end");
            String query_strand = (String) record.get("strand");

            // get the number of middle point and flank_point
            ArrayList<Integer> data_point_list = DataPointNum.getDataPointNum();
            int num_middle_point = data_point_list.get(0);
            int num_flank_point = data_point_list.get(1);
            // config code need to change when flank_factor exist
            if (flank_factor > 0){
                flank_size = (end_pos - start_pos + 1)*flank_factor;
            } else {
                flank_size = flanking_size;
            }
            // Specify the genome interval range to calculate the coverage
            Interval interval_range = QueryGenomeRange.getQueryBamGranges(chr_name,
                    start_pos, end_pos, flank_size);
            System.out.println("interval_range: " + interval_range);
            int query_length = interval_range.getEnd() - interval_range.getStart() + 1;

            // calculate the physical coverage for each query region
            ArrayList<Integer> physical_coverage =
                    PhysicalCoverageCalculator.calculatePhysicalCoverage(bam_file_path, interval_range, query_strand);
            /*
            # If paired, filter reads that are not properly paired.
            paired <- all(with(srg, is.na(isize) | isize != 0))
            if(paired) {
                p.mask <- with(srg, rname == mrnm & xor(strand == '+', isize < 0))
                all.mask <- all.mask & p.mask
            }
             */

            // Filter transcripts whose chromosomes do not match bam file.

            // remove the buffers from the coverage
            physical_coverage = TrimBuffer.trimBuffer(physical_coverage, buf_size);

            // scale the coverage into the data point for plot
            DataScaler data_scaler = new DataScaler(physical_coverage);
            List<Double> coverage_scaled = data_scaler.getCoverageScaled(scaler_method);
            // if strand is `-` , reverse the coverage
            if(query_strand.equals("-")){
                System.out.println("query strand: " + query_strand);
                System.out.println("reverse the scaled coverage. " );
                Collections.reverse(coverage_scaled);
            }
            // (done in the spline method) Floor negative values which are caused by spline.


            System.out.println(coverage_scaled);
            System.out.println("finishing record: " + record);
            return coverage_scaled;
        }
        // identify the middle point coordinate for the point interval of query gene
        private static int getMiddlePointPos(int start_pos, int end_pos, String strand) {
            int middle_point;
            boolean cond_1 = region_plot.equals("tss") && strand.equals("+");
            boolean cond_2 = region_plot.equals("tes") && strand.equals("-");
            if ( cond_1 || cond_2){
                middle_point = start_pos;
            } else {
                middle_point = end_pos;
            }
            return middle_point;
        }


}
