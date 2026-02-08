package com.NGSViz.sqldbOperate;

import com.NGSViz.ReadBam.QueryGenomeRange;
import com.NGSViz.configSet.InputParameterAttributes;
import com.NGSViz.coverageCalculator.EachCoverageResult;
import com.NGSViz.coverageCalculator.PhysicalCoverageCalculator;
import com.NGSViz.coverageCalculator.TrimBuffer;
import com.NGSViz.coverageCalculator.DataScaler;
import htsjdk.samtools.util.Interval;
import com.NGSViz.sqldbOperate.exonMode.CoverageExonSubset;
import com.NGSViz.sqldbOperate.exonMode.ExonModelData;
import htsjdk.samtools.SamReader;

/**
 * @author Benchen Ye
 * @create 2024-10--16:16
 * @function calculate the scaled coverage of large ChIP for each query region (record)
 */
public class ProcessEachQueryCoorRecord {
    private static String region_plot = InputParameterAttributes.region_type;
    private static int flanking_size = InputParameterAttributes.flank_region;
    private static int buf_size = InputParameterAttributes.buf_size;
    private static String bin_method = InputParameterAttributes.bin_method;
    private static double flank_factor = InputParameterAttributes.flank_factor;
    private static String interval_type = InputParameterAttributes.interval_type;

    // [CHANGED] Thread-safe per-record context for chunk-level single BAM scan.
    public static class RecordContext {
        public final Transcript transcript;
        public final String queryStrand;
        public final String tidName;
        public final int flankSize;
        public final Interval intervalRange;

        public RecordContext(Transcript transcript, String queryStrand, String tidName, int flankSize, Interval intervalRange) {
            this.transcript = transcript;
            this.queryStrand = queryStrand;
            this.tidName = tidName;
            this.flankSize = flankSize;
            this.intervalRange = intervalRange;
        }
    }

    // [CHANGED] Keep original API, but delegate to context-based logic.
    public static EachCoverageResult processRecord2(Transcript gene_coord, SamReader bam_reader,
                                                    String chr_name) {
        RecordContext ctx = buildRecordContext(gene_coord, chr_name);
        int[] physical_coverage =
                PhysicalCoverageCalculator.calculatePhysicalCoverage2(bam_reader, ctx.intervalRange,
                        ctx.queryStrand);
        return processRecordFromPhysicalCoverage(gene_coord, physical_coverage, ctx);
    }

    // [CHANGED] Build interval/flank as local values (no shared mutable static state).
    public static RecordContext buildRecordContext(Transcript gene_coord, String chr_name) {
        int flank_size;
        Interval interval_range;

        String query_strand = gene_coord.getQueryStrand();
        String record_name = gene_coord.getRecordName();
        int start_pos = gene_coord.getStartPos();
        int end_pos = gene_coord.getEndPos();
        String[] parts = record_name.split(":");
        String tid_name = parts.length > 1 ? parts[1] : "";

        if (flank_factor > 0) {
            if (interval_type.equals("exon")) {
                double calculate_res = ExonModelData.getEnstExonWidth(tid_name) * flank_factor;
                flank_size = (int) calculate_res;
            } else {
                double calculate_res = (end_pos - start_pos + 1) * flank_factor;
                flank_size = (int) calculate_res;
            }
        } else {
            flank_size = flanking_size;
        }

        if (interval_type.equals("point_interval")) {
            int middle_point = getMiddlePointPos(start_pos, end_pos, query_strand);
            interval_range = QueryGenomeRange.getQueryBamGranges(chr_name, middle_point);
        } else if (interval_type.equals("exon")) {
            System.out.println("----\n");
            System.out.println("--- Performing exon mode! ---");
            start_pos = ExonModelData.getEnstStart(tid_name);
            end_pos = ExonModelData.getEnstEnd(tid_name);
            interval_range = QueryGenomeRange.getQueryBamGranges(chr_name,
                    start_pos, end_pos, flank_size);
        } else {
            start_pos = gene_coord.getStartPos();
            end_pos = gene_coord.getEndPos();
            interval_range = QueryGenomeRange.getQueryBamGranges(chr_name,
                    start_pos, end_pos, flank_size);
        }

        return new RecordContext(gene_coord, query_strand, tid_name, flank_size, interval_range);
    }

    // [CHANGED] Post-processing entry for chunk-level scan result.
    public static EachCoverageResult processRecordFromPhysicalCoverage(
            Transcript gene_coord,
            int[] physical_coverage,
            RecordContext ctx
    ) {
        if (interval_type.equals("exon")) {
            int query_range_start = ctx.intervalRange.getStart();
            int query_range_end = ctx.intervalRange.getEnd();
            physical_coverage = CoverageExonSubset.getCoverageExonSubset(physical_coverage, ctx.tidName,
                    query_range_start, query_range_end);
        }

        physical_coverage = TrimBuffer.trimBuffer2(physical_coverage, buf_size);
        double[] coverage_scaled = DataScaler.getCoverageScaled(physical_coverage, bin_method, ctx.flankSize);

        if (ctx.queryStrand.equals("-")) {
            reverseDoubleArray(coverage_scaled);
        }

        return new EachCoverageResult(gene_coord, coverage_scaled);
    }

    public static void reverseDoubleArray(double[] array) {
        if (array == null || array.length <= 1) {
            return;
        }
        int left = 0;
        int right = array.length - 1;
        while (left < right) {
            double temp = array[left];
            array[left] = array[right];
            array[right] = temp;
            left++;
            right--;
        }
    }

    private static int getMiddlePointPos(int start_pos, int end_pos, String strand) {
        int middle_point;
        boolean cond_1 = region_plot.equals("tss") && strand.equals("+");
        boolean cond_2 = region_plot.equals("tes") && strand.equals("-");
        if (cond_1 || cond_2) {
            middle_point = start_pos;
        } else {
            middle_point = end_pos;
        }
        return middle_point;
    }
}
