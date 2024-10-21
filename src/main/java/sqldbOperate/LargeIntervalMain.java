package sqldbOperate;

import ReadBam.ReadBam;
import configSet.CommonFinalParas;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Benchen Ye
 * @create 2024-10--18:28
 */
public class LargeIntervalMain {
    private static int num_datapoints = CommonFinalParas.num_datapoints;
    private static int num_core = CommonFinalParas.cores_num;

    public static void main(String[] args) {
        String query_refname = "hg19.ensembl.genebody.protein_coding";
        List<Map<String, Object>> results = SQLiteQuery.queryGenomeCoorDatabaseRecord(query_refname);

        /*
        // initiation coverage_scaled_sum list
        List<Double> coverage_scaled_sum = new ArrayList<>();
        for (int i = 0; i < num_datapoints; i++) {
            coverage_scaled_sum.add(0.0);
        }
         */
        // loop results list and add the coverage_scaled
        for (int i = 0; i < results.size(); i++) {
            Map<String, Object> record = results.get(i);
            String chr_name = (String) record.get("chrom");

            // check whether is the bowtie
            //if(bowtie) {srg = within(srg, mapq[is.na(mapq)] <- 254)}

            boolean chromosome_in_bam = ReadBam.checkChromosomeInBam(chr_name);
            if (chromosome_in_bam) {
                LargeChIPProcessRecordGenomeCoordinateDB.processRecord(record);
            } else {
                System.out.println(chr_name + " isn't in the bam");
            }

            // add the coverage_scaled to coverage_scaled_sum
            /*
            for (int j = 0; j < coverage_scaled.size(); j++) {
                coverage_scaled_sum.set(j, coverage_scaled_sum.get(j) + coverage_scaled.get(j));
            }
            //System.out.println("Coverage Scaled Sum: " + coverage_scaled_sum);

             */
        }
        // output the result
        //System.out.println("Coverage Scaled Sum: " + coverage_scaled_sum);
    }
}
