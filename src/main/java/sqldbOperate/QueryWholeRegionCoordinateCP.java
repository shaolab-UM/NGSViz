package sqldbOperate;

import java.sql.*;
import java.util.*;

/**
 * @author Benchen Ye
 * @create 2024-11--19:32
 * @function get the whole query region coordinate
 */
public class QueryWholeRegionCoordinateCP extends DBAtribute{
    public static List<Double> width_list = new ArrayList<>();
    public static List<String> unique_chrname_list = new ArrayList<>();
    public static List<String> unique_nochrname_list = new ArrayList<>();
    public static List<String> DB_gene_list = new ArrayList<>();
    public static List<Map<String, Object>> query_coord = new ArrayList<>();
    public static int region_num = 0;

    public static void main(String[] args) {
        String tbl_name = "Homo_sapiens_ensembl_GRCh38_113";
        String biotype = "protein_coding";
        String type = "transcript";
        queryGenomeCoorDatabaseRecord(tbl_name, biotype, type);
    }
    public static void queryGenomeCoorDatabaseRecord(String tbl_name, String biotype, String type) {
        // can use the chunk to improve the performance
        List<Map<String, Object>> results = new ArrayList<>();
        try {
            initialDB();
            String query = String.format("SELECT *  FROM '%s' WHERE biotype = '%s' AND type = '%s'",
                    tbl_name, biotype, type);
            System.out.println("The query keyword is : " + query);
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                region_num++;
                Map<String, Object> record = new HashMap<>();
                // get the items from database table with getString/getInt
                String record_name = resultSet.getString("gname") + ":" +
                        resultSet.getString("tid");
                record.put("record_name", record_name);
                String chr_name = resultSet.getString("chrom");
                String gene_name = resultSet.getString("gname");
                // remove the 'chr' of chromosome name
                String nochr_name = chr_name.replace("chr", "");
                record.put("chrom", chr_name);
                record.put("nochr_name", nochr_name);
                record.put("gene_name", gene_name);
                if (!unique_chrname_list.contains(chr_name)) {
                    unique_chrname_list.add(chr_name);
                    unique_nochrname_list.add(nochr_name);
                }
                if (!DB_gene_list.contains(gene_name)){
                    DB_gene_list.add(gene_name);
                }
                record.put("start", resultSet.getInt("start"));
                record.put("end", resultSet.getInt("end"));
                record.put("strand", resultSet.getString("strand"));
                int width = resultSet.getInt("end") - resultSet.getInt("start") +1;
                width_list.add((double) width);
                query_coord.add(record);
            }
        }catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
        exitDB();

    }
}
