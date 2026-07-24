package com.NGSViz.ReadBam;

import com.NGSViz.configSet.InputParameterAttributes;
import htsjdk.samtools.util.Interval;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for QueryGenomeRange interval boundary handling.
 *
 * htsjdk Interval uses 1-based closed coordinates.
 * When flanking regions push query_start below 1, it must be clamped to 1,
 * not 0, to avoid IllegalArgumentException from Interval constructor.
 */
class QueryGenomeRangeTest {

    @BeforeEach
    void setDefaults() {
        InputParameterAttributes.buf_size = 10;
        InputParameterAttributes.flank_region = 50;
        InputParameterAttributes.region_type = "gene_body";
        InputParameterAttributes.interval_type = "broad_interval";
        InputParameterAttributes.flank_factor = 0.0;
    }

    @Test
    void testQueryStartClampedToOneWhenFlankExceedsStart() {
        // Gene at [1, 100] with flank=50 and buf=10
        // query_start = 1 - 50 - 10 = -59 -> must clamp to 1, not 0
        Interval iv = QueryGenomeRange.getQueryBamGranges("chr1", 1, 100, 50);
        assertEquals(1, iv.getStart(),
                "When flanking exceeds gene start, query_start must be clamped to 1 (1-based minimum)");
        assertEquals(100 + 50 + 10, iv.getEnd());
    }

    @Test
    void testQueryStartAtGeneStart() {
        // Gene at [100, 200] with flank=10 and buf=5
        // query_start = 100 - 10 - 5 = 85 (valid, no clamping needed)
        InputParameterAttributes.buf_size = 5;
        InputParameterAttributes.flank_region = 10;

        Interval iv = QueryGenomeRange.getQueryBamGranges("chr1", 100, 200, 10);
        assertEquals(85, iv.getStart());
        assertEquals(215, iv.getEnd());
    }

    @Test
    void testPointIntervalStartClampedToOne() {
        // Middle point at 5 with flank=50 and buf=10
        // query_start = 5 - 50 - 10 = -55 -> must clamp to 1
        InputParameterAttributes.buf_size = 10;
        InputParameterAttributes.flank_region = 50;

        Interval iv = QueryGenomeRange.getQueryBamGranges("chr1", 5);
        assertEquals(1, iv.getStart(),
                "Point interval query_start must be clamped to 1 when it would go below 1");
    }

    @Test
    void testIntervalConstructorDoesNotThrowForStartEqualsOne() {
        // This is a sanity check: Interval accepts start=1, but rejects start=0
        assertDoesNotThrow(() -> new Interval("chr1", 1, 100),
                "htsjdk Interval must accept start=1");
    }
}
