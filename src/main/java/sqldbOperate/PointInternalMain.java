package sqldbOperate;

import DataPrepare.CurrentTime;
import configSet.CommonFinalParas;
import coverageCalculator.AverageCoverage;
import coverageCalculator.AverageCoverage;
import coverageCalculator.Matrix2CSV;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Benchen Ye
 * @create 2024-10--20:47
 */

public class PointInternalMain {
    private static int num_datapoints = CommonFinalParas.num_datapoints;
    private static int trim_ratio = CommonFinalParas.robust;
    private static String current_time = CurrentTime.getCurrentTime();
    private static String heatmap_data_path = "/Users/bencheye/myProj/ngsPlot/Output/coverage_matrix_heatmap.csv";
    private static String average_coverage_path = "/Users/bencheye/myProj/ngsPlot/Output/average_coverage_matrix.csv";

    public static void main(String[] args) {
        String query_refname = "hg19.ensembl.genebody.protein_coding";
        List<Map<String, Object>> query_regions = SQLiteQuery.queryGenomeCoorDatabaseRecord(query_refname);
        int num_query_regions = query_regions.size();
        System.out.println("The number of the genes in the query regions is " + num_query_regions);
        // initiation coverage_scaled_matrix (heatmap data)
        double[][] coverage_scaled_matrix = new double[num_query_regions][num_datapoints+1];
        // save the row names of the coverage_scaled_matrix
        List<String> row_names = new ArrayList<>();
        // loop results list and add the coverage_scaled
        for (int i = 0; i < num_query_regions; i++) {
            Map<String, Object> record = query_regions.get(i);
            String record_name = record.get("record_name").toString();
            row_names.add(record_name);
            System.out.println("The query region name is " + record_name);
            List<Double> coverage_scaled = PintProcessRecordGenomeCoordinateDB.processRecord(record);
            // add the coverage_scaled to coverage_scaled_matrix
            for (int j = 0; j < coverage_scaled.size(); j++) {
                coverage_scaled_matrix[i][j] = coverage_scaled.get(j);
            }
            //System.out.println("Coverage Scaled Sum: " + coverage_scaled_sum);
        }

        // whether is the pair-end bam

        // Calculate SEM if needed. Shut off SEM in single gene case.

        // output the result
        System.out.println("Coverage Scaled Matrix finish!");
        System.out.println("The number of row is: " + coverage_scaled_matrix.length);
        System.out.println("The number of column is: " + coverage_scaled_matrix[0].length);

        // Return avg. profile.
        double[] average_coverage_matrix = AverageCoverage.calculateAverageCoverage(coverage_scaled_matrix, trim_ratio);
        // save the data

        Matrix2CSV.saveMatrix2CSV(coverage_scaled_matrix, row_names, heatmap_data_path);
        Matrix2CSV.saveMatrix2CSV(average_coverage_matrix, average_coverage_path);

    }
}

/*
public class Main {
    private static int num_datapoints = CommonFinalParas.num_datapoints;
    private static int num_core = CommonFinalParas.cores_num;

    public static void main(String[] args) {
        String query_refname = "hg19.ensembl.genebody.protein_coding";
        List<Map<String, Object>> results = SQLiteQuery.queryGenomeCoorDatabaseRecord(query_refname);
        // list size
        int list_size = num_datapoints + 1;
        // AtomicReferenceArray<DoubleAdder> represents a thread-safe reference array where each element is of type DoubleAdder
        AtomicReferenceArray<DoubleAdder> coverage_scaled_sum = new AtomicReferenceArray<>(list_size);
        for (int i = 0; i < list_size; i++) {
            coverage_scaled_sum.set(i, new DoubleAdder());
        }
        // ForkJoinPool is a thread pool for parallel task execution in Java,
        // especially for partitioning algorithms and recursive tasks.
        ForkJoinPool customThreadPool = new ForkJoinPool(num_core);
        customThreadPool.submit(() ->
                results.parallelStream().forEach(record -> {
                    List<Double> coverage_scaled = PintProcessRecordGenomeCoordinateDB.processRecord(record);
                    for (int i = 0; i < list_size; i++) {
                        coverage_scaled_sum.get(i).add(coverage_scaled.get(i));
                    }
                })
        ).join();

        System.out.println("coverage scaled sum is : " + coverage_scaled_sum);

        // output the sum
        for (int i = 0; i < list_size; i++) {
            System.out.println("Index " + i + ": " + coverage_scaled_sum.get(i).sum());
        }
    }
}

 */

/*
    public static void main(String[] args) {
        String query_refname = "hg19.ensembl.genebody.protein_coding";
        List<Map<String, Object>> results = SQLiteQuery.queryGenomeCoorDatabaseRecord(query_refname);

        // set core num
        ForkJoinPool customThreadPool = new ForkJoinPool(4);
        customThreadPool.submit(() ->
                results.parallelStream().forEach(PintProcessRecordGenomeCoordinateDB::processRecord)
        ).join();
    }

     */