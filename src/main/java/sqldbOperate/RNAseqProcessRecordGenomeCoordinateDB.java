package sqldbOperate;

import ReadBam.QueryGenomeRange;
import configSet.InputParameterAttributes;
import coverageCalculator.PhysicalCoverageCalculator;
import coverageCalculator.TrimBuffer;
import coverageCalculator.dataScaler.DataScaler;
import htsjdk.samtools.util.Interval;
import sqldbOperate.exonMode.CoverageExonSubset;
import sqldbOperate.exonMode.ExonModelData;
import sqldbOperate.exonMode.OneEnstAllExonCoorClass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Benchen Ye
 * @create 2024-11--17:36
 */
public class RNAseqProcessRecordGenomeCoordinateDB {
    private static String region_plot = InputParameterAttributes.region_plot;
    private static int flanking_size = InputParameterAttributes.flank_region;
    private static int buf_size = InputParameterAttributes.buf_size;
    private static String scaler_method = InputParameterAttributes.scaler_method;
    private static double flank_factor = InputParameterAttributes.flank_factor;
    private static int flank_size;
    private static String analysis_type = InputParameterAttributes.analysis_type;

    public static List<Double> processRecord(Map<String, Object> record, String bam_file_path, String chr_name){
        System.out.println("Processing record: " + record);
        // genome coordinate information
        int start_pos = (int) record.get("start");
        int end_pos = (int) record.get("end");
        String query_strand = (String) record.get("strand");
        String record_name = (String) record.get("record_name");
        String[] parts = record_name.split(":");
        String tid_name = parts.length > 1 ? parts[1] : "";
        // get the number of middle point and flank_point
        int num_middle_point = InputParameterAttributes.middle_points;
        int num_flank_point = InputParameterAttributes.flank_points;
        // config code need to change when flank_factor exist

        if (flank_factor > 0){
            // RNA-seq specific
            double calculate_res = ExonModelData.getEnstExonWidth(tid_name)*flank_factor;
            flank_size = (int) calculate_res;
        } else {
            flank_size = flanking_size;
        }
        // RNA-seq
        start_pos = ExonModelData.getEnstStart(tid_name);
        end_pos = ExonModelData.getEnstEnd(tid_name);

        // Specify the genome interval range to calculate the coverage
        Interval interval_range = QueryGenomeRange.getQueryBamGranges(chr_name,
                start_pos, end_pos, flank_size);
        int query_range_start = interval_range.getStart();
        int query_range_end = interval_range.getEnd();
        System.out.println("interval_range: " + interval_range);
        // calculate the physical coverage for each query region
        ArrayList<Integer> physical_coverage =
                PhysicalCoverageCalculator.calculatePhysicalCoverage(bam_file_path, interval_range, query_strand);
        System.out.println("---------");
        System.out.println("---------");
        System.out.println("tid_name: " + tid_name);
        OneEnstAllExonCoorClass tid_res = (OneEnstAllExonCoorClass) ExonModelData.exon_matrix.get(tid_name);
        System.out.println("exon res is : " + tid_res);
        System.out.println("start list is :" + tid_res.getStartList());
        System.out.println("physical coverage is: " + physical_coverage);
        System.out.println("---------");
        System.out.println("---------");
        if(analysis_type.equals("rnaseq")){
            // process exon coverage
            physical_coverage = CoverageExonSubset.getCoverageExonSubset(physical_coverage, tid_name,
                    query_range_start, query_range_end);
        }
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
}
