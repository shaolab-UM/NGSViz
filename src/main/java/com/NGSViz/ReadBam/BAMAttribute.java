package com.NGSViz.ReadBam;


import com.NGSViz.configSet.InputParameterAttributes;
import htsjdk.samtools.*;
import htsjdk.samtools.util.CloseableIterator;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BAMAttribute {
    private static String bam_file_path;
    private static Set<String> bam_chromosomes_list;
    private final boolean isPairedEnd;
    private final long library_size;
    private final boolean chromosomeFormat;
    private static SAMFileHeader header;
    private static File bamFile;

    public BAMAttribute(String bam_file_path) throws IOException {
        BAMAttribute.bam_file_path = bam_file_path;
        System.out.println("--- The bam file will be analysing: " + bam_file_path);
        System.out.println("--- get bam file information ---");

        bamFile = checkBamFile(bam_file_path);
        SamReaderFactory factory = SamReaderFactory.makeDefault();

        try (SamReader bamReader = factory.open(bamFile);) {
            validateBamForIndexedQueries(bamReader, bamFile);
            // Retrieve chromosome list
            bam_chromosomes_list = getChromosomesFromHeader(bamReader);
            LibraryStats stats = calculateLibraryStats(bamReader);
            this.library_size = stats.getLibrarySize();
            this.isPairedEnd = stats.isPairedEnd();
            header = bamReader.getFileHeader();
            this.chromosomeFormat = stats.getChromosomeFormat();
            InputParameterAttributes.bamChromLabStartWithChr = this.chromosomeFormat;
            InputParameterAttributes.header = header;
        } catch (NoSuchElementException e) {
            throw new IOException("Failed to read BAM file: " + e.getMessage(), e);
        }
    }

    private static void validateBamForIndexedQueries(
            SamReader bamReader,
            File bamFile
    ) throws IOException {
        if (!bamReader.hasIndex()) {
            throw new IOException(
                    "BAM index not found for '" + bamFile.getAbsolutePath()
                            + "'. Create a BAI or CSI index with: samtools index "
                            + bamFile.getAbsolutePath()
            );
        }
        SAMFileHeader.SortOrder sortOrder = bamReader.getFileHeader().getSortOrder();
        if (sortOrder != SAMFileHeader.SortOrder.coordinate) {
            throw new IOException(
                    "BAM file must be coordinate-sorted for indexed interval queries: '"
                            + bamFile.getAbsolutePath() + "'. Header sort order is '"
                            + sortOrder + "'."
            );
        }
        try {
            bamReader.indexing().getIndex();
        } catch (RuntimeException e) {
            throw new IOException(
                    "BAM index is unreadable or incompatible with '"
                            + bamFile.getAbsolutePath() + "'.",
                    e
            );
        }
    }

    /*
    * methods
    * */
    public static File checkBamFile (String bam_file_path) {
        File bamFile = new File(bam_file_path);
        // check whether the bam file exist
        if (!bamFile.exists()) {
            System.err.println("Error: The BAM file" + bam_file_path + " does not exist!");
            System.exit(1);
        }
        return bamFile;
    }
    // Calculate the library size and determine the sequencing type of the internal class
    private static class LibraryStats {
        private final long librarySize;
        private final boolean isPairedEnd;
        private final boolean chromosomeFormat;

        public LibraryStats(long librarySize, boolean isPairedEnd, boolean chromosomeFormat) {
            this.librarySize = librarySize;
            this.isPairedEnd = isPairedEnd;
            this.chromosomeFormat = chromosomeFormat;
        }

        public long getLibrarySize() {
            return librarySize;
        }

        public boolean isPairedEnd() {
            return isPairedEnd;
        }

        public boolean getChromosomeFormat(){
            return chromosomeFormat;
        }
    }

    // Calculate the library size and determine the sequencing type.
    private LibraryStats calculateLibraryStats(SamReader bamReader) {
        long librarySize = 0;
        boolean isPairedEnd = false;
        boolean firstRecord = true;
        boolean chromosomeFormat = false;

        try (CloseableIterator<SAMRecord> iterator = bamReader.iterator()) {
            while (iterator.hasNext()) {
                SAMRecord samRecord = iterator.next();

                // Determine the sequencing type (only check the first record)
                if (firstRecord) {
                    isPairedEnd = samRecord.getReadPairedFlag();
                    System.out.println("This BAM file is " +
                            (isPairedEnd ? "paired-end." : "single-end."));
                    String firstChromosome = samRecord.getReferenceName();
                    chromosomeFormat = detectChromosomeFormat(firstChromosome);

                    firstRecord = false;
                }

                // Only count reads that pass quality control, are non-repetitive, and have been compared.
                if (samRecord.getMappingQuality() >= 0 &&
                        !samRecord.getDuplicateReadFlag() &&
                        !samRecord.getReadUnmappedFlag()) {
                    librarySize++;
                }
            }
        }

        return new LibraryStats(librarySize, isPairedEnd, chromosomeFormat);
    }

    // Detect chromosome format
    private boolean detectChromosomeFormat(String chromosome) {
        boolean chromosomeFormat = false;
        if (chromosome == null || chromosome.isEmpty()) {
            System.out.println("Unknown chromosome label format!");
            System.exit(1);
        }
        if (chromosome.startsWith("chr")) {
            System.out.println("Chromosome label format is 'chr'!");
            chromosomeFormat = true;
        } else if (chromosome.matches("^[0-9XYM]+$")) {
            System.out.println("Chromosome label is Pure numeric format!");
        } else {
            System.out.println("Unknown chromosome label format!");
            System.exit(1);
        }
        return chromosomeFormat;
    }

    // get the chromosome list in bam file
    private static Set<String> getChromosomesFromHeader(SamReader bamReader) {
        Set<String> chromosomes = new LinkedHashSet<>();
        SAMFileHeader header = bamReader.getFileHeader();
        if (header.getSequenceDictionary() != null) {
            for (SAMSequenceRecord sequenceRecord : header.getSequenceDictionary().getSequences()) {
                chromosomes.add(sequenceRecord.getSequenceName());
            }
        }
        return Collections.unmodifiableSet(chromosomes);
    }


    // Getters
    public long getLibrarySize() {
        return library_size;
    }

    public Set<String> getBamChromosomesList() {
        return bam_chromosomes_list;
    }

    public boolean getChromosomeFormat(){
        return chromosomeFormat;
    }

    public boolean getPairedEndMode(){
        return isPairedEnd;
    }

    public SAMFileHeader getHeader(){
        return header;
    }

    public File getBamFile(){
        return bamFile;
    }

}
