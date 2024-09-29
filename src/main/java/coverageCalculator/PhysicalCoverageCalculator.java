package coverageCalculator;

import configSet.CommonFinalParas;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.util.Interval;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Benchen Ye
 * @create 2024-09--22:25
 */
public class PhysicalCoverageCalculator {

    // calculate the physical coverage for each query region
    public static ArrayList<Integer> calculatePhysicalCoverage(String bam_file_path, Interval intervalRange) {
        // use final para*
        int min_mapq = CommonFinalParas.min_mapq;
        int frag_len = CommonFinalParas.frag_len;

        File bamFile = new File(bam_file_path);
        // check whether the bam file exist
        if (!bamFile.exists()) {
            System.err.println("Error: The BAM file" + bam_file_path + " does not exist!");
            System.exit(1);
        }
        // create the SamReader class
        SamReaderFactory factory = SamReaderFactory.makeDefault();
        SamReader bam_reader = factory.open(bamFile);
        //
        String chr_name = intervalRange.getContig();
        int query_range_start = intervalRange.getStart();
        int query_range_end = intervalRange.getEnd();
        // use the MultiValueMap to save the alignment information
        //calculate.coverageMatrix.MultiValueMap<String, Object> readInfoMap = new MultiValueMap<>();
        // Get the alignment results. `queryContained` conduct stricter query, only the reads fully included in the region
        SAMRecordIterator iterator = bam_reader.queryContained(chr_name, query_range_start, query_range_end);
        // gene range (length in the genome)
        int range_len = query_range_end - query_range_start + 1;
        System.out.println("Query length: " + range_len);
        // create a list (length = rangeLen, value = 0)
        ArrayList<Integer> physical_coverage = new ArrayList<>(Collections.nCopies(range_len, 0));
        while (iterator.hasNext()) {
            SAMRecord record = iterator.next();
            ReadRecordProcess record_obj = new ReadRecordProcess(record);
            // filter the bam alignment for each read
            boolean filterRes = record_obj.filterOneReadAlign();
            System.out.println(filterRes);
            // bam filter
            if (! filterRes) {
                List<Integer> readAlignInfo = record_obj.getReadAlignmentInfo();
                System.out.println(readAlignInfo);
                int align_pos_start =readAlignInfo.get(0);
                int qwidth = readAlignInfo.get(1);
                // read start position in the search range
                // index start from zero, so no add 1
                align_pos_start = align_pos_start - query_range_start;
                // calculate the physical coverage
                physical_coverage = physicalCoverageCalculate(physical_coverage, align_pos_start, qwidth);
            }
        }
        // calculate the data point coverage
        return physical_coverage;
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
        for (int i = align_start_pos; i < align_start_pos + read_width; i++) {
            // add 1 for the value of i
            physical_coverage.set(i, physical_coverage.get(i) + 1);
        }
        return physical_coverage;
    }

}
