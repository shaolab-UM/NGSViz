import htsjdk.samtools.BAMIndexer;
import htsjdk.samtools.SamReaderFactory;
import java.io.File;

class IndexBam {
    public static void main(String[] args) throws Exception {
        // 为测试 BAM 建立独立索引，不修改原始数据。
        try (var reader = SamReaderFactory.makeDefault()
                .enable(SamReaderFactory.Option.INCLUDE_SOURCE_IN_RECORDS)
                .open(new File(args[0]))) {
            BAMIndexer.createIndex(reader, new File(args[1]));
        }
    }
}
