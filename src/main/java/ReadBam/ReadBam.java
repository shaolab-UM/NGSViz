package ReadBam;


import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Benchen Ye
 * @create 2024-10--18:56
 */
public class ReadBam {
    // test

    public static SamReader getBamReader (String bam_file_path) {
        File bam_file = new File(bam_file_path);
        String bam_name = bam_file.getName();
        // check whether the bam file exist
        if (!bam_file.exists()) {
            System.err.println("Error: The BAM file" + bam_file_path + " does not exist!");
            System.exit(1);
        }
        // create the SamReader class
        SamReaderFactory factory = SamReaderFactory.makeDefault();
        System.out.println("Read the bam file: " + bam_name);
        SamReader bam_reader = factory.open(bam_file);
        return bam_reader;
    }
    public static void closeBamReader (SamReader bam_reader) {
        try {
            bam_reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // get all the unique chromosome numbers
    public static Set<String> getBamUniqCharList(String bam_file_path) {
        // get the bam header
        SamReader bam_reader = ReadBam.getBamReader(bam_file_path);
        SAMFileHeader header = bam_reader.getFileHeader();
        // Get all unique chromosome numbers
        Set<String> chromosomes = new HashSet<>();
        for (SAMSequenceRecord sequence_record : header.getSequenceDictionary().getSequences()) {
            chromosomes.add(sequence_record.getSequenceName());
        }
        return chromosomes;
    }

    // get the chromosome list in bam file
    public static Set<String> getChromosomesInBam(String bam_file_path) {
        Set<String> chromosomes = ReadBam.getBamUniqCharList(bam_file_path);
        return chromosomes;
    }

    public static String checkChromosomeInBam(Set<String> chromosomes, String chromosome_2_check, String nochrName_2_check) {
        String check_result = null;
        // check whether the chromosome is in Set
        if (chromosomes.contains(chromosome_2_check)) {
            check_result = chromosome_2_check;
        } else if (chromosomes.contains(nochrName_2_check)) {
            check_result = nochrName_2_check;
        } else {
            System.out.println(chromosome_2_check + " is not in the set.");
        }
        return check_result;
    }
}
