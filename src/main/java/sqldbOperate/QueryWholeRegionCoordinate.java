package sqldbOperate;

import configSet.InputParameterAttributes;
import configSet.ProcessInputParameters;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Benchen Ye
 * @create 2024-11--19:32
 * @function get the whole query region coordinate
 */
public class QueryWholeRegionCoordinate extends DBAtribute{
    public static List<Double> width_list = new ArrayList<>();
    public static List<String> unique_chrname_list = new ArrayList<>();
    public static List<String> unique_nochrname_list = new ArrayList<>();
    public static List<Map<String, Object>> query_coord = new ArrayList<>();
    public static int region_num = 0;

    public static void queryGenomeCoorDatabaseRecord(String species, String refname) {
        // can use the chunk to improve the performance
        List<Map<String, Object>> results = new ArrayList<>();
        try {
            initialDB();
            String query = String.format("SELECT *  FROM '%s' WHERE refname = '%s'", species, refname);
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
                // remove the 'chr' of chromosome name
                String nochr_name = chr_name.replace("chr", "");
                record.put("chrom", chr_name);
                record.put("nochr_name", nochr_name);
                if (!unique_chrname_list.contains(chr_name)) {
                    unique_chrname_list.add(chr_name);
                    unique_nochrname_list.add(nochr_name);
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
        //System.out.println("query results is: " + results);
        exitDB();
        //return results;
    }
}
