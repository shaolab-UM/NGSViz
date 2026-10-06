import com.NGSViz.configSet.InputParameterAttributes;
import com.NGSViz.coverageCalculator.PhysicalCoverageCalculator;
import com.NGSViz.sqldbOperate.ProcessEachQueryCoorRecord.RecordContext;
import com.NGSViz.sqldbOperate.Transcript;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.util.Interval;
import java.io.BufferedReader;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

class VerifyRaw {
    public static void main(String[] args) throws Exception {
        // 按实验设计固定片段长度与筛选参数。
        InputParameterAttributes.frag_len = 20;
        InputParameterAttributes.min_mapq = 20;
        InputParameterAttributes.strand_spec = "both";
        var transcript = new Transcript("synthetic_full:NM_SYNTHETIC", "synthetic_full",
                "NM_SYNTHETIC", "chrSynthetic", "Synthetic", "+", 1, 500);
        var context = new RecordContext(transcript, "+", "NM_SYNTHETIC", 0,
                new Interval("chrSynthetic", 1, 500));
        int[] coverage;
        int readCount;
        try (var reader = SamReaderFactory.makeDefault().open(new File(args[0]))) {
            var result = PhysicalCoverageCalculator.calculatePhysicalCoverageBatch(reader, List.of(context));
            coverage = result.getCoverageByRecord().get(transcript.getIndex());
            readCount = result.getReadCountByRecord().get(transcript.getIndex());
        }
        int mismatches = 0;
        int maxError = 0;
        long totalError = 0;
        try (BufferedReader expected = Files.newBufferedReader(Path.of(args[1]))) {
            for (int i = 0; i < 500; i++) {
                String[] row = expected.readLine().split("\t");
                if (!row[0].equals("chrSynthetic") || Integer.parseInt(row[1]) != i + 1)
                    throw new IllegalArgumentException("Expected coverage coordinates are invalid.");
                int error = Math.abs(coverage[i] - Integer.parseInt(row[2]));
                if (error != 0) mismatches++;
                maxError = Math.max(maxError, error);
                totalError += error;
            }
            if (expected.readLine() != null) throw new IllegalArgumentException("Expected coverage has extra rows.");
        }
        System.out.printf("positions=500 reads=%d mismatches=%d max_error=%d MAE=%.6f%n",
                readCount, mismatches, maxError, totalError / 500.0);
        if (args.length > 2) {
            // Save the computed base-level coverage for figure generation.
            var output = new StringBuilder("chrom\tposition\tcoverage\n");
            for (int i = 0; i < coverage.length; i++)
                output.append("chrSynthetic\t").append(i + 1).append('\t').append(coverage[i]).append('\n');
            Files.writeString(Path.of(args[2]), output.toString());
        }
        if (readCount != 18 || mismatches != 0) System.exit(1);
    }
}
