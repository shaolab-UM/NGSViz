package com.NGSViz.coverageCalculator;

import com.NGSViz.sqldbOperate.ProcessEachQueryCoorRecord.RecordContext;
import com.NGSViz.sqldbOperate.Transcript;
import htsjdk.samtools.*;
import htsjdk.samtools.util.Interval;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PhysicalCoverageCalculatorTest {
    @TempDir Path tempDir;

    @Test
    void countsProperPairOnceWhenBothReadsOverlap() throws Exception {
        SAMFileHeader header = new SAMFileHeader();
        header.setSortOrder(SAMFileHeader.SortOrder.coordinate);
        header.addSequence(new SAMSequenceRecord("chr1", 1000));
        Path bam = tempDir.resolve("pair.bam");
        try (SAMFileWriter writer = new SAMFileWriterFactory()
                .setCreateIndex(true).makeBAMWriter(header, true, bam.toFile())) {
            writer.addAlignment(read(header, 100, 150, 60, true));
            writer.addAlignment(read(header, 150, 100, -60, false));
        }

        Transcript transcript = new Transcript("gene:tx", "gene", "tx", "chr1", "1", "+", 100, 159);
        RecordContext context = new RecordContext(transcript, "+", "tx", 0, new Interval("chr1", 100, 159));
        try (SamReader reader = SamReaderFactory.makeDefault().open(bam.toFile())) {
            PhysicalCoverageCalculator.BatchCoverageResult result =
                    PhysicalCoverageCalculator.calculatePhysicalCoverageBatch(reader, List.of(context));
            assertEquals(1, result.getReadCountByRecord().get(transcript.getIndex()));
            assertEquals(1, result.getCoverageByRecord().get(transcript.getIndex())[0]);
            assertEquals(1, result.getCoverageByRecord().get(transcript.getIndex())[59]);
        }
    }

    private SAMRecord read(SAMFileHeader header, int start, int mateStart, int tlen, boolean first) {
        SAMRecord record = new SAMRecord(header);
        record.setReadName("pair");
        record.setReferenceName("chr1");
        record.setAlignmentStart(start);
        record.setCigarString("10M");
        record.setReadString("AAAAAAAAAA");
        record.setBaseQualityString("IIIIIIIIII");
        record.setMappingQuality(60);
        record.setReadPairedFlag(true);
        record.setProperPairFlag(true);
        record.setFirstOfPairFlag(first);
        record.setSecondOfPairFlag(!first);
        record.setMateReferenceName("chr1");
        record.setMateAlignmentStart(mateStart);
        record.setInferredInsertSize(tlen);
        record.setReadNegativeStrandFlag(!first);
        record.setMateNegativeStrandFlag(first);
        return record;
    }
}
