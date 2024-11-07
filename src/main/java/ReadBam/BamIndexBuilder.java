package ReadBam;

import configSet.CommonFinalParas;
import htsjdk.samtools.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Benchen Ye
 * @create 2024-11--17:16
 */
public class BamIndexBuilder {
    private static String bam_file_path = CommonFinalParas.bam_file;
    public static void xx(String[] args) {
        try {
            SamReaderFactory readerFactory = SamReaderFactory.makeDefault().validationStringency(ValidationStringency.SILENT);
            SamReader reader = readerFactory.open(new File(bam_file_path));
            // the output path of bam index file
            Path index_output_path = new File(bam_file_path + ".bai").toPath();
            // build the bam index with BAMIndexer
            BAMIndexer.createIndex(reader, index_output_path);
            System.out.println("BAM index file has been created");
            reader.close();
        } catch (IOException e) {
            System.err.println("there is a error when build bam index：" + e.getMessage());
        }
    }


}