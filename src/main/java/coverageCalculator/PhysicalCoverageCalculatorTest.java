package coverageCalculator;

import ReadBam.ReadBam;
import ReadBam.ReadRecordProcess;
import ReadBam.QueryGenomeRange;
import configSet.ProcessInputParameters;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.util.Interval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Benchen Ye
 * @create 2024-09--22:25
 * @function calculate the physical coverage for one query region
 */
public class PhysicalCoverageCalculatorTest {
    public static void main(String[] args) {
        String bam_file_path = "/Users/bencheye/myProj/ngsPlot/Data/hesc.RNAseq.1M.bam";
        Interval interval_range;
        String query_strand = "-";
        boolean paired_mode = false;
        String chr_name = "chr1";
        int start_pos = 879583;
        int end_pos = 894636;
        int flank_size = 2000;

        interval_range = QueryGenomeRange.getQueryBamGranges(chr_name, start_pos, end_pos, flank_size);
        System.out.println(interval_range);
        //interval_range = QueryGenomeRange.getQueryBamGranges(chr_name, middle_point);
        ArrayList<Integer> res = calculatePhysicalCoverage(bam_file_path, interval_range, query_strand, paired_mode);
        System.out.println(res);

    }
    // calculate the physical coverage for each query region
    public static ArrayList<Integer> calculatePhysicalCoverage(String bam_file_path,
                                                               Interval interval_range,
                                                               String query_strand,
                                                               boolean paired_mode)
    {
        String chr_name = interval_range.getContig();
        int query_range_start = interval_range.getStart();
        int query_range_end = interval_range.getEnd();
        // use the MultiValueMap to save the alignment information
        // Get the alignment results. `queryContained` conduct stricter query, only the reads fully included in the region
        //System.out.println("Get all reads fully included in the query interval region!");
        SamReader bam_reader = ReadBam.getBamReader(bam_file_path);
        SAMRecordIterator iterator = bam_reader.queryContained(chr_name, query_range_start, query_range_end);
        // gene range (length in the genome)
        int range_len = query_range_end - query_range_start + 1;
        // create a list (length = rangeLen, value = 0)
        ArrayList<Integer> coverage = new ArrayList<>(Collections.nCopies(range_len, 0));
        System.out.println("Start to calculate physical coverage for all reads in query interval region!");
        while (iterator.hasNext()) {
            SAMRecord bam_record = iterator.next();
            // filter the bam alignment for each read, true filter, false not filter
            boolean filterRes = ReadRecordProcess.filterOneReadAlign(bam_record, query_strand);
            boolean paired_lab = ReadRecordProcess.checkPairedRecord(bam_record);
            // paired mood
            if(paired_mode){
                if (paired_lab) {
                    System.out.println("--- paired mode ---");
                    System.out.println("paired bam, there exists insert region!");
                    boolean paired_filter = ReadRecordProcess.checkPairedRecordQuality(bam_record);
                    filterRes = filterRes & !paired_filter;
                } else {
                    System.out.println("paired bam error, there not exists insert region!");
                }
            }
            // bam filter
            if (! filterRes) {
                // get the alignment read information
                List<Integer> read_align_info;
                if(paired_mode & paired_lab){
                    read_align_info = ReadRecordProcess.getPairedReadAlignmentInfo(bam_record);
                } else {
                    read_align_info = ReadRecordProcess.getReadAlignmentInfo(bam_record, interval_range);
                }
                if (read_align_info.get(0) != 0) {
                    System.out.println("--- Processing a read: " + read_align_info);
                    int align_pos_start = read_align_info.get(0);
                    int align_width = read_align_info.get(1);
                    // read start position in the search range
                    // index start from zero, so no add 1
                    int pos_start_difference = align_pos_start - query_range_start;
                    // paired mode mate read induce value less than 0
                    if(pos_start_difference<0){
                        pos_start_difference = 0;
                        align_width = align_width - Math.abs(pos_start_difference);
                    } else if(pos_start_difference>range_len){
                        pos_start_difference = range_len;
                        align_width = 0;
                    }
                    // paired mode tlen greater than length of query interval range
                    if(align_width<0){
                        align_width = 0;
                    } else if(align_width > range_len){
                        align_width = range_len;
                    }
                    //System.out.println("the width of align segment: " + align_width);
                    if((align_width + pos_start_difference) > range_len){
                        align_width = range_len - pos_start_difference;
                    }
                    // calculate the physical coverage
                    System.out.println("the coverage start position for this alignment: " + pos_start_difference);
                    System.out.println("the width of align segment: " + align_width);
                    System.out.println("Query length for interval region is: " + range_len);
                    coverage = physicalCoverageCalculate(coverage, pos_start_difference, align_width);
                }
            }
        }
        System.out.println("Finish calculating physical coverage in query region: " + interval_range);
        ReadBam.closeBamReader (bam_reader);
        // calculate the data point coverage
        return coverage;
    }

    // calculate the physical coverage for a reads in query range
    // Input:
    //   physical_coverage, physical coverage of query region
    //   align_start_pos: the start position of read
    //   read_width: the width of read
    // Output: physical_coverage, the value of each the query range covered by read will add 1
    public static ArrayList<Integer> physicalCoverageCalculate(ArrayList<Integer> physical_coverage,
                                                        int align_start_pos,
                                                        int read_width) {
        // the coverage value of the region covered by read coverage will add 1
        for (int i = align_start_pos; i < (align_start_pos + read_width); i++) {
            // add 1 for the value of i
            physical_coverage.set(i, physical_coverage.get(i) + 1);
        }
        return physical_coverage;
    }
}
