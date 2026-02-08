package com.NGSViz.coverageCalculator;

import com.NGSViz.ReadBam.QueryGenomeRange;
import com.NGSViz.ReadBam.ReadRecordProcess;
import com.NGSViz.configSet.InputParameterAttributes;
import com.NGSViz.sqldbOperate.ProcessEachQueryCoorRecord;
import htsjdk.samtools.*;
import htsjdk.samtools.util.Interval;

import java.util.*;

/**
 * @author Benchen Ye
 * @create 2024-09--22:25
 * @function calculate the physical coverage for one query region
 */
public class PhysicalCoverageCalculator {

    private static final int frag_len = InputParameterAttributes.frag_len;

    // [CHANGED] Batch result now contains both coverage vectors and read counts per transcript.
    public static class BatchCoverageResult {
        private final Map<Integer, int[]> coverageByRecord;
        private final Map<Integer, Integer> readCountByRecord;

        public BatchCoverageResult(Map<Integer, int[]> coverageByRecord, Map<Integer, Integer> readCountByRecord) {
            this.coverageByRecord = coverageByRecord;
            this.readCountByRecord = readCountByRecord;
        }

        public Map<Integer, int[]> getCoverageByRecord() {
            return coverageByRecord;
        }

        public Map<Integer, Integer> getReadCountByRecord() {
            return readCountByRecord;
        }
    }

    public static int[] calculatePhysicalCoverage2(SamReader bam_reader,
                                                   Interval interval_range,
                                                   String query_strand)
    {
        String chr_name = interval_range.getContig();
        int query_range_start = interval_range.getStart();
        int query_range_end = interval_range.getEnd();
        int range_len = query_range_end - query_range_start + 1;

        try (SAMRecordIterator iterator = bam_reader.queryContained(chr_name, query_range_start, query_range_end)) {
            int[] coverageDiff = new int[range_len + 1];
            while (iterator.hasNext()) {
                SAMRecord bam_record = iterator.next();
                boolean filterRes = ReadRecordProcess.filterOneReadAlign(bam_record, query_strand);
                if (filterRes) continue;

                boolean isPairedEnd = bam_record.getReadPairedFlag();
                if (bam_record.getReadUnmappedFlag()) continue;
                List<Integer> read_align_info;
                if (isPairedEnd) {
                    // [CHANGED] Count one end per proper pair to avoid double counting fragments.
                    if (!bam_record.getProperPairFlag() || !bam_record.getFirstOfPairFlag()) continue;
                    read_align_info = ReadRecordProcess.getPairedReadAlignmentInfo(bam_record);
                } else {
                    read_align_info = ReadRecordProcess.getReadAlignmentInfo(bam_record, interval_range);
                }

                if (read_align_info.get(0) != 0) {
                    int align_pos_start = read_align_info.get(0);
                    int align_width = read_align_info.get(1);
                    int current_align_end = align_pos_start + align_width;

                    int overlap_start = Math.max(align_pos_start, query_range_start);
                    int overlap_end = Math.min(current_align_end, query_range_end);

                    int effective_width = overlap_end - overlap_start;

                    if (effective_width > 0) {
                        int pos_start_in_coverage_array = overlap_start - query_range_start;
                        int pos_end_in_coverage_array = pos_start_in_coverage_array + effective_width;
                        if (pos_start_in_coverage_array >= 0 && pos_end_in_coverage_array <= range_len) {
                            coverageDiff[pos_start_in_coverage_array]++;
                            coverageDiff[pos_end_in_coverage_array]--;
                        }
                    }
                }
            }
            int[] coverage = new int[range_len];
            int running = 0;
            for (int i = 0; i < range_len; i++) {
                running += coverageDiff[i];
                coverage[i] = running;
            }
            return coverage;
        }
        catch (Exception e) {
            System.err.println("Error creating iterator for " + chr_name + ":" + query_range_start + "-" + query_range_end + ": " + e.getMessage());
            throw new RuntimeException("Failed to process interval: " + e.getMessage(), e);
        }
    }

    // [CHANGED] Chunk-level single scan: merge intervals, scan once, dispatch reads.
    public static BatchCoverageResult calculatePhysicalCoverageBatch(
            SamReader bam_reader,
            List<ProcessEachQueryCoorRecord.RecordContext> contexts
    ) {
        Map<Integer, int[]> coverageMap = new HashMap<>();
        Map<Integer, Integer> readCountMap = new HashMap<>();
        if (contexts == null || contexts.isEmpty()) {
            return new BatchCoverageResult(coverageMap, readCountMap);
        }

        List<Interval> intervals = new ArrayList<>(contexts.size());
        Map<Integer, int[]> coverageDiffByRecord = new HashMap<>(contexts.size());

        for (ProcessEachQueryCoorRecord.RecordContext ctx : contexts) {
            int rangeLen = ctx.intervalRange.getEnd() - ctx.intervalRange.getStart() + 1;
            coverageDiffByRecord.put(ctx.transcript.getIndex(), new int[rangeLen + 1]);
            readCountMap.put(ctx.transcript.getIndex(), 0);
            intervals.add(ctx.intervalRange);
        }

        QueryInterval[] mergedIntervals = QueryGenomeRange.mergeIntervals(intervals, bam_reader.getFileHeader());

        try (SAMRecordIterator iterator = bam_reader.query(mergedIntervals, false)) {
            while (iterator.hasNext()) {
                SAMRecord bamRecord = iterator.next();

                if (bamRecord.getReadUnmappedFlag()) continue;
                // [CHANGED] Count one end per proper pair to avoid double counting fragments.
                if (bamRecord.getReadPairedFlag() &&
                        (!bamRecord.getProperPairFlag() || !bamRecord.getFirstOfPairFlag())) continue;

                int[] readSpan = getReadSpan(bamRecord);
                if (readSpan == null) continue;
                int readStart = readSpan[0];
                int readEndExclusive = readSpan[1];

                for (ProcessEachQueryCoorRecord.RecordContext ctx : contexts) {
                    Interval interval = ctx.intervalRange;
                    if (!interval.getContig().equals(bamRecord.getReferenceName())) {
                        continue;
                    }

                    int queryStart = interval.getStart();
                    int queryEndExclusive = interval.getEnd() + 1;
                    int overlapStart = Math.max(readStart, queryStart);
                    int overlapEnd = Math.min(readEndExclusive, queryEndExclusive);
                    if (overlapEnd <= overlapStart) {
                        continue;
                    }

                    boolean filterRes = ReadRecordProcess.filterOneReadAlign(bamRecord, ctx.queryStrand);
                    if (filterRes) {
                        continue;
                    }

                    int posStart = overlapStart - queryStart;
                    int posEnd = overlapEnd - queryStart;
                    int[] diff = coverageDiffByRecord.get(ctx.transcript.getIndex());
                    if (posStart >= 0 && posEnd <= diff.length - 1) {
                        diff[posStart]++;
                        diff[posEnd]--;
                        // [CHANGED] Count reads that physically overlap this transcript interval.
                        readCountMap.merge(ctx.transcript.getIndex(), 1, Integer::sum);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed batch coverage scan: " + e.getMessage(), e);
        }

        for (ProcessEachQueryCoorRecord.RecordContext ctx : contexts) {
            int[] diff = coverageDiffByRecord.get(ctx.transcript.getIndex());
            int[] coverage = new int[diff.length - 1];
            int running = 0;
            for (int i = 0; i < coverage.length; i++) {
                running += diff[i];
                coverage[i] = running;
            }
            coverageMap.put(ctx.transcript.getIndex(), coverage);
        }

        return new BatchCoverageResult(coverageMap, readCountMap);
    }

    // [CHANGED] Read span extraction without per-gene range dependency.
    private static int[] getReadSpan(SAMRecord bamRecord) {
        int alignStart;
        int readWidth;

        if (bamRecord.getReadPairedFlag()) {
            List<Integer> alignInfo = ReadRecordProcess.getPairedReadAlignmentInfo(bamRecord);
            alignStart = alignInfo.get(0);
            readWidth = alignInfo.get(1);
        } else {
            alignStart = bamRecord.getAlignmentStart();
            int readLength = bamRecord.getReadLength();
            if (bamRecord.getReadNegativeStrandFlag()) {
                alignStart = alignStart + readLength - frag_len;
            }
            readWidth = frag_len;
        }

        if (readWidth <= 0) {
            return null;
        }

        return new int[]{alignStart, alignStart + readWidth};
    }

    public static int[] physicalCoverageCalculate(int[] physical_coverage,
                                                  int align_start_pos,
                                                  int read_width) {
        if (align_start_pos < 0 || align_start_pos + read_width > physical_coverage.length) {
            throw new IllegalArgumentException("Invalid range: align_start_pos=" + align_start_pos +
                    ", read_width=" + read_width + ", array length=" + physical_coverage.length);
        }

        for (int i = align_start_pos; i < align_start_pos + read_width; i++) {
            physical_coverage[i]++;
        }
        return physical_coverage;
    }
}
