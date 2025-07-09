package com.NGSViz;

import java.io.File;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import com.NGSViz.sqldbOperate.ParallelGeneDbProcessor;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.util.Interval;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.*;

import static com.NGSViz.coverageCalculator.PhysicalCoverageCalculator.calculatePhysicalCoverage2;

/*public class TestRun {

    public static String db_path = "/Users/benche/myProj/ngsPlot/NGSViz/database/genomeCoordinate.db";
    public static final String DATABASE_URL = "jdbc:sqlite:" + db_path;
    public static Connection connection = null;
    public static Statement statement = null;
    public static String tbl_name = "hg19_RefSeq";
    public static String biotype = "protein_coding";
    public static String type = "transcript";

    public static void main(String[] args) throws IOException {
        // 2. 实例化 GeneDbProcessor 并调用其处理方法
        //File bamFile = new File("/Users/benche/myProj/ngsPlot/Data/LX/sgControl_LSD2.bam");
        File bamFile = new File("/Users/benche/myProj/ngsPlot/Data/GSE209153_H3K4me3_sample/GSE209153_H3K4me3_0.5.sorted.bam");

        // 创建 SamReader
        SamReaderFactory factory = SamReaderFactory.makeDefault();
        SamReader reader = factory.open(bamFile);
        String query_strand = "+";
        boolean paired_mode = false;
        Interval interval_range = new Interval("14", 20467256, 20479239);

        calculatePhysicalCoverage2( reader, interval_range, query_strand,  paired_mode);

    }*/
    import htsjdk.samtools.*;

import java.io.File;

import htsjdk.samtools.*;

import java.io.File;

public class QuickPairedEndCheck {
    public static void main(String[] args) {
        File bamFile = new File("/Users/benche/myProj/ngsPlot/Data/GSE209153_H3K4me3_sample/GSE209153_H3K4me3_0.5.sorted.bam");
        try (SamReader reader = SamReaderFactory.makeDefault().open(bamFile)) {
            SAMRecord firstRecord = reader.iterator().next();
            if (firstRecord.getReadPairedFlag()) {
                System.out.println("This BAM file is paired-end.");
            } else {
                System.out.println("This BAM file is single-end.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
