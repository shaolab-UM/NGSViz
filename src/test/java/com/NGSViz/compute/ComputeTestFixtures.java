package com.NGSViz.compute;

import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMFileWriter;
import htsjdk.samtools.SAMFileWriterFactory;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMSequenceRecord;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

final class ComputeTestFixtures {
    private ComputeTestFixtures() {
    }

    static Path writeDatabase(Path path, String coordinateTable) throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + path);
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE defaultTbl (Species TEXT, CoordinateTblName TEXT, "
                    + "DB TEXT, Genome TEXT, Region TEXT, AnalysisType TEXT, Biotype TEXT, "
                    + "PointLab TEXT, FlankSize REAL)");
            statement.execute("INSERT INTO defaultTbl VALUES ('Homo_sapiens', '"
                    + coordinateTable + "', 'RefSeq', 'hg38', 'tss', 'transcript', "
                    + "'protein_coding', 'TSS', 2000)");
            if (coordinateTable.equals("hg38_RefSeq")) {
                statement.execute("CREATE TABLE hg38_RefSeq (chrom TEXT, start INTEGER, "
                        + "end INTEGER, width INTEGER, strand TEXT, type TEXT, biotype TEXT, "
                        + "gname TEXT, tid TEXT, DefaultChoose REAL)");
                statement.execute("INSERT INTO hg38_RefSeq VALUES "
                        + "('chr1', 100, 200, 100, '+', 'transcript', 'protein_coding', "
                        + "'GENE1', 'TX1', 1)");
            }
        }
        return path;
    }

    static Path writeSystemConfig(Path path, Path database) throws IOException {
        JSONObject config = new JSONObject();
        config.put("versionNum", new JSONObject().put("version", "1.3"));
        config.put("toolParas", new JSONObject()
                .put("tool_path", path.getParent().toString())
                .put("db_path", database.toString())
                .put("tool_name", "NGSViz-1.0.jar")
                .put("java_path", System.getProperty("java.home") + "/bin/java"));
        Files.writeString(path, config.toString(2));
        return path;
    }

    static Path writeBam(Path path, String reference, boolean createIndex) {
        SAMFileHeader header = new SAMFileHeader();
        header.setSortOrder(SAMFileHeader.SortOrder.coordinate);
        header.addSequence(new SAMSequenceRecord(reference, 1000));
        SAMRecord record = new SAMRecord(header);
        record.setReadName("read1");
        record.setReferenceName(reference);
        record.setAlignmentStart(1);
        record.setCigarString("10M");
        record.setReadString("AAAAAAAAAA");
        record.setBaseQualityString("IIIIIIIIII");
        record.setMappingQuality(60);
        try (SAMFileWriter writer = new SAMFileWriterFactory()
                .setCreateIndex(createIndex).makeBAMWriter(header, true, path.toFile())) {
            writer.addAlignment(record);
        }
        return path;
    }

    static JSONObject validRequest(Path root, Path config, Path signalBam) {
        return new JSONObject()
                .put("schema_version", "1.3")
                .put("sample_id", "sample-1")
                .put("sample_name", "Sample 1")
                .put("group_name", "Group A")
                .put("title", "Sample_1_TSS")
                .put("genome", "hg38")
                .put("region", "tss")
                .put("signal_bam", signalBam.toString())
                .put("control_bam", JSONObject.NULL)
                .put("output_dir", root.resolve("output").toString())
                .put("system_config", config.toString())
                .put("database", "RefSeq")
                .put("analysis_type", "transcript")
                .put("biotype", "protein_coding")
                .put("num_datapoints", 100);
    }
}
