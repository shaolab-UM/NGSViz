package coverageCalculator;

import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.util.Interval;
import ReadBam.ReadBam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import ReadBam.ReadRecordProcess;
import sqldbOperate.exonMode.ExonModelData;
import sqldbOperate.exonMode.OneEnstAllExonCoorClass;

/**
 * @author Benchen Ye
 * @create 2024-09--22:25
 * @function calculate the physical coverage for one query region
 */
public class PhysicalCoverageCalculator {
    // calculate the physical coverage for each query region
    public static ArrayList<Integer> calculatePhysicalCoverage(String bam_file_path,
                                                               Interval interval_range,
                                                               String query_strand)
    {
        //
        String chr_name = interval_range.getContig();
        int query_range_start = interval_range.getStart();
        int query_range_end = interval_range.getEnd();
        // use the MultiValueMap to save the alignment information
        // Get the alignment results. `queryContained` conduct stricter query, only the reads fully included in the region
        System.out.println("Get all reads fully included in the query region: " + interval_range);
        SamReader bam_reader = ReadBam.getBamReader(bam_file_path);
        SAMRecordIterator iterator = bam_reader.queryContained(chr_name, query_range_start, query_range_end);
        // gene range (length in the genome)
        int range_len = query_range_end - query_range_start + 1;
        System.out.println("Query length: " + range_len);
        // create a list (length = rangeLen, value = 0)
        ArrayList<Integer> coverage = new ArrayList<>(Collections.nCopies(range_len, 0));
        System.out.println("Start to calculate physical coverage for all reads in query region: " + interval_range);
        while (iterator.hasNext()) {
            SAMRecord record = iterator.next();
            // filter the bam alignment for each read
            boolean filterRes = ReadRecordProcess.filterOneReadAlign(record, query_strand);
            // bam filter
            if (! filterRes) {

                // If paired, filter reads that are not properly paired.
                //        paired <- all(with(srg, is.na(isize) | isize != 0))
                //        if(paired) {
                //            p.mask <- with(srg, rname == mrnm & xor(strand == '+', isize < 0))
                //            all.mask <- all.mask & p.mask
                //        }

                //if(paired) {
                //                cov.pos <- with(srg, ifelse(isize < 0, mpos, pos))
                //                cov.wd <- abs(srg$isize)
                //            } else {
                //                # Adjust negative read positions for physical coverage.
                //                cov.pos <- with(srg, ifelse(strand == '-',
                //                                            pos - fraglen + qwidth, pos))
                //                cov.wd <- fraglen
                //            }

                // get the alignment read information
                List<Integer> readAlignInfo = ReadRecordProcess.getReadAlignmentInfo(record, interval_range);
                if (readAlignInfo.get(0) != 0) {
                    System.out.println("Processing a read: " + readAlignInfo);
                    int align_pos_start = readAlignInfo.get(0);
                    int qwidth = readAlignInfo.get(1);
                    // read start position in the search range
                    // index start from zero, so no add 1
                    align_pos_start = align_pos_start - query_range_start;
                    // calculate the physical coverage
                    coverage = physicalCoverageCalculate(coverage, align_pos_start, qwidth);
                }
            }
        }
        System.out.println("Finish calculating physical coverage in query region: " + interval_range);
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
        for (int i = align_start_pos; i < align_start_pos + read_width; i++) {
            // add 1 for the value of i
            physical_coverage.set(i, physical_coverage.get(i) + 1);
        }
        return physical_coverage;
    }
}
