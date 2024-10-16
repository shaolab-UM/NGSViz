package coverageCalculator;

import configSet.CommonFinalParas;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.filter.DuplicateReadFilter;
import htsjdk.samtools.filter.MappingQualityFilter;
import htsjdk.samtools.filter.SamRecordFilter;
import htsjdk.samtools.util.Interval;

import java.util.Arrays;
import java.util.List;

/**
 * @author Benchen Ye
 * @create 2024-09--22:38
 */
public class ReadRecordProcess {
    // get the genome position information of read in the bam
    // Input:
    //   record: SAMRecord object, a record of a read including the query range
    //   frag_len: fragment length
    // Output: read_align_info, an integer list includes two item, align_start_pos and read_width
    //   align_start_pos, the start position of read; read_width, the width of read
    public static List<Integer> getReadAlignmentInfo(SAMRecord record, Interval interval_range){
        int frag_len = CommonFinalParas.frag_len;
        // get the alignment value, the start position and width of read
        int align_start_pos = record.getAlignmentStart();
        int read_width = record.getReadLength();
        int query_range_start = interval_range.getStart();
        int query_range_end = interval_range.getEnd();

        System.out.println("The align start pos of this read is " + align_start_pos);
        // Negative Strand Flag
        boolean strand_flag = record.getReadNegativeStrandFlag();
        System.out.println("The strand flag of this read is " + strand_flag);
        if (strand_flag){
            // if strand's direction is "-", adjust the start position of read
            align_start_pos = align_start_pos - frag_len + read_width;
        }
        int align_end_pos = align_start_pos + read_width;
        if (align_start_pos < query_range_start || align_end_pos > query_range_end){
            System.out.println("The adjusted - read is not in the query range!");
            System.out.println("The adjusted - read read is " + align_start_pos + " - " + align_end_pos);
            return Arrays.asList(0);
        }

        List<Integer> read_align_info;
        read_align_info = Arrays.asList(align_start_pos, read_width);
        return read_align_info;
    }

    // filter the bam alignment information for one read (duplicated read, unmapped reads, low alignment quality)
    // Input:
    //   record: SAMRecord object, a record of a read including the query range
    //   min_mapq: the read will be filtered lower than the bam quality
    // Output: filter_res, true (read needed to be filtered)/false (read will be not filter)
    public static boolean filterOneReadAlign(SAMRecord record){
        int min_mapq = CommonFinalParas.min_mapq;

        boolean filter_res = false;
        // create the filter
        SamRecordFilter filter1 = new DuplicateReadFilter();
        SamRecordFilter filter2 = new MappingQualityFilter(min_mapq);
        if(filter1.filterOut(record)){
            filter_res = true; // Duplicate Read
        }
        if(filter2.filterOut(record)){
            filter_res = true; // low alignment quality
        }
        if (record.getReadUnmappedFlag()) {
            filter_res = true; // Read Unmapped
        }

        if (filter_res) {
            System.out.println("This bam read do not pass the quality, will be filted!");
            System.out.println("This bam read is: ");
        }
        return filter_res;
    }
}
