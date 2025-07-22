package com.NGSViz.ReadBam;


import com.NGSViz.configSet.InputParameterAttributes;
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
 * @function
 */
public class ReadRecordProcess {
    private static int frag_len = InputParameterAttributes.frag_len;
    private static int min_mapq = InputParameterAttributes.min_mapq;
    private static String strand_spec = InputParameterAttributes.strand_spec;
    // get the genome position information of read in the bam
    // Input:
    //   record: SAMRecord object, a record of a read including the query range
    //   frag_len: fragment length
    // Output: read_align_info, an integer list includes two item, align_start_pos and read_width
    //   align_start_pos, the start position of read; read_width, the width of read
    public static List<Integer> getReadAlignmentInfo(SAMRecord bam_record, Interval interval_range){
        // get the alignment value, the start position and width of read
        int align_start_pos = bam_record.getAlignmentStart();
        int read_width = bam_record.getReadLength();
        int query_range_start = interval_range.getStart();
        int query_range_end = interval_range.getEnd();
        //System.out.println("The align start pos of this read is " + align_start_pos);
        // Negative Strand Flag
        boolean strand_flag = bam_record.getReadNegativeStrandFlag();
        //System.out.println("The strand flag of this read is " + strand_flag);
        if (strand_flag){
            // if strand's direction is "-", adjust the start position of read
            align_start_pos = align_start_pos - frag_len + read_width;
        }
        int align_end_pos = align_start_pos + read_width;
        if (align_start_pos < query_range_start || align_end_pos > query_range_end){
            return Arrays.asList(0);
        }
        List<Integer> read_align_info;
        read_align_info = Arrays.asList(align_start_pos, read_width);
        return read_align_info;
    }

    public static List<Integer> getPairedReadAlignmentInfo(SAMRecord bam_record){
        // adjust paired interval range
        int align_start_pos;
        int read_width;
        // the length of insert (width)
        int tlen = bam_record.getInferredInsertSize();
        // get the alignment value, the start position and width of read
        int pos = bam_record.getAlignmentStart();
        int mpos = bam_record.getMateAlignmentStart();
        if(tlen<0){
            align_start_pos = mpos;
        }else{
            align_start_pos = pos;
        }
        read_width = Math.abs(tlen);
        List<Integer> read_align_info;
        read_align_info = Arrays.asList(align_start_pos, read_width);
        return read_align_info;
    }

    public static boolean checkPairedRecord(SAMRecord bam_record){
        boolean paired_mood = false;
        // the length of insert
        int tlen = bam_record.getInferredInsertSize();
        //System.out.println("--- the tlen is: " + tlen);
        if(tlen != 0){
            paired_mood  = true;
        }
        return paired_mood;
    }
     public static boolean checkPairedRecordQuality(SAMRecord bam_record){
        // the length of insert
        int tlen = bam_record.getInferredInsertSize();
        String rname = bam_record.getReferenceName();
        String mrnm = bam_record.getMateReferenceName();
        boolean strand_flag = bam_record.getReadNegativeStrandFlag();
        String read_strand;
        if (strand_flag){
             read_strand = "-";
        } else {
             read_strand = "+";
        }
        boolean condition1 = rname.equals(mrnm);
        boolean condition2 = read_strand.equals("+");
        boolean condition3 = tlen<0;
        boolean condition_xor = condition2 ^ condition3;
        boolean paired_res = condition1 & condition2;
        return paired_res;
     }

    // filter the bam alignment information for one read (duplicated read, unmapped reads, low alignment quality)
    // Input:
    //   record: SAMRecord object, a record of a read including the query range
    //   min_mapq: the read will be filtered lower than the bam quality
    // Output: filter_res, true (read needed to be filtered)/false (read will be not filter)
    public static boolean filterOneReadAlign(SAMRecord record, String query_strand){
        String read_strand;
        boolean strand_flag = record.getReadNegativeStrandFlag();
        if (strand_flag){
            read_strand = "-";
        } else {
            read_strand = "+";
        }
        boolean filter_res = false;
        // create the filter
        SamRecordFilter filter1 = new DuplicateReadFilter();
        SamRecordFilter filter2 = new MappingQualityFilter(min_mapq);
        // Duplicate Read
        if(filter1.filterOut(record)){
            filter_res = true;
        }
        // low alignment quality
        if(filter2.filterOut(record)){
            filter_res = true;
        }
        // Read Unmapped
        if (record.getReadUnmappedFlag()) {
            filter_res = true;
        }
        // filter strand - OPTIMIZED: use equals() instead of ==
        if(!"both".equals(strand_spec)){
            if("same".equals(strand_spec)){
                // the direction of strand is not same will be filtered (True)
                filter_res = !query_strand.equals(read_strand);
            } else {
                // the direction of strand is same will be filtered (True)
                filter_res = query_strand.equals(read_strand);
            }
        }
        return filter_res;
    }
}
