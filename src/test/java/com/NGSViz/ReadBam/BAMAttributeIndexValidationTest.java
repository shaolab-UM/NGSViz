package com.NGSViz.ReadBam;

import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMFileWriter;
import htsjdk.samtools.SAMFileWriterFactory;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMSequenceRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BAMAttributeIndexValidationTest {

    @TempDir
    Path tempDir;

    @Test
    void rejectsCoordinateSortedBamWithoutIndex() {
        File bamFile = writeBam(
                tempDir.resolve("missing-index.bam").toFile(),
                SAMFileHeader.SortOrder.coordinate,
                false
        );

        IOException error = assertThrows(
                IOException.class,
                () -> new BAMAttribute(bamFile.getAbsolutePath())
        );

        assertTrue(error.getMessage().contains("index"));
        assertTrue(error.getMessage().contains(bamFile.getAbsolutePath()));
    }

    @Test
    void rejectsBamNotDeclaredCoordinateSorted() throws IOException {
        File indexedBam = writeBam(
                tempDir.resolve("indexed.bam").toFile(),
                SAMFileHeader.SortOrder.coordinate,
                true
        );
        File unsortedBam = writeBam(
                tempDir.resolve("unsorted.bam").toFile(),
                SAMFileHeader.SortOrder.unsorted,
                false
        );
        Files.copy(
                htsjdk.samtools.SamFiles.findIndex(indexedBam).toPath(),
                tempDir.resolve("unsorted.bam.bai")
        );

        IOException error = assertThrows(
                IOException.class,
                () -> new BAMAttribute(unsortedBam.getAbsolutePath())
        );

        assertTrue(error.getMessage().contains("coordinate-sorted"));
        assertTrue(error.getMessage().contains("unsorted"));
    }

    @Test
    void rejectsUnreadableIndex() throws IOException {
        File bamFile = writeBam(
                tempDir.resolve("corrupt-index.bam").toFile(),
                SAMFileHeader.SortOrder.coordinate,
                false
        );
        Files.write(tempDir.resolve("corrupt-index.bam.bai"), new byte[]{0, 1, 2, 3});

        IOException error = assertThrows(
                IOException.class,
                () -> new BAMAttribute(bamFile.getAbsolutePath())
        );

        assertTrue(error.getMessage().contains("unreadable or incompatible"));
        assertTrue(error.getMessage().contains(bamFile.getAbsolutePath()));
    }

    @Test
    void acceptsCoordinateSortedBamWithReadableIndex() {
        File bamFile = writeBam(
                tempDir.resolve("valid-index.bam").toFile(),
                SAMFileHeader.SortOrder.coordinate,
                true
        );

        assertDoesNotThrow(() -> new BAMAttribute(bamFile.getAbsolutePath()));
    }

    private File writeBam(
            File bamFile,
            SAMFileHeader.SortOrder sortOrder,
            boolean createIndex
    ) {
        SAMFileHeader header = new SAMFileHeader();
        header.setSortOrder(sortOrder);
        header.addSequence(new SAMSequenceRecord("chr1", 1000));

        SAMRecord record = new SAMRecord(header);
        record.setReadName("read1");
        record.setReferenceName("chr1");
        record.setAlignmentStart(100);
        record.setCigarString("10M");
        record.setReadString("AAAAAAAAAA");
        record.setBaseQualityString("IIIIIIIIII");
        record.setMappingQuality(60);

        SAMFileWriterFactory factory = new SAMFileWriterFactory();
        factory.setCreateIndex(createIndex);
        try (SAMFileWriter writer = factory.makeBAMWriter(header, true, bamFile)) {
            writer.addAlignment(record);
        }
        return bamFile;
    }
}
