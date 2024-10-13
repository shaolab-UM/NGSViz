package coverageCalculator;

import configSet.CommonFinalParas;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.filter.DuplicateReadFilter;
import htsjdk.samtools.filter.MappingQualityFilter;
import htsjdk.samtools.filter.SamRecordFilter;

import java.util.Arrays;
import java.util.List;

/**
 * @author Benchen Ye
 * @create 2024-09--22:38
 */
public class ReadRecordProcess {
    private SAMRecord record;

    ReadRecordProcess(SAMRecord record) {
        this.record = record;
    }

    // get the genome position information of read in the bam
    // Input:
    //   record: SAMRecord object, a record of a read including the query range
    //   frag_len: fragment length
    // Output: read_align_info, an integer list includes two item, align_start_pos and read_width
    //   align_start_pos, the start position of read; read_width, the width of read
    public List<Integer> getReadAlignmentInfo(){
        int frag_len = CommonFinalParas.frag_len;
        // get the alignment value, the start position and width of read
        int align_start_pos = record.getAlignmentStart();
        int read_width = record.getReadLength();
        // record.getMappingQuality();
        boolean strand_flag = record.getReadNegativeStrandFlag(); // Negative Strand Flag

        if (strand_flag){
            // if strand's direction is "-", adjust the start position of read
            align_start_pos = align_start_pos - frag_len + read_width;
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
    public boolean filterOneReadAlign(){
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
        // filter paired read with picard CountingPairedFilter
        //
        return filter_res;
    }
}
