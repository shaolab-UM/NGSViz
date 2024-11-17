package sqldbOperate;

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
    public static void main(String [] args){
        String species = "human";
        String refname = "hg19.ensembl.genebody.protein_coding";
        List<Map<String, Object>> res = queryGenomeCoorDatabaseRecord(species, refname);
        //System.out.println("res is " + res);
    }
    public static List<Map<String, Object>> queryGenomeCoorDatabaseRecord(String species, String refname) {
        // can use the chunk to improve the performance
        List<Map<String, Object>> results = new ArrayList<>();
        try {
            initialDB();
            String query = String.format("SELECT *  FROM '%s' WHERE refname = '%s'", species, refname);
            System.out.println("The query keyword is : " + query);
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                Map<String, Object> record = new HashMap<>();
                // get the items from database table with getString/getInt
                String record_name = resultSet.getString("gname") + ":" +
                        resultSet.getString("tid");
                record.put("record_name", record_name);
                record.put("chrom", resultSet.getString("chrom"));
                record.put("start", resultSet.getInt("start"));
                record.put("end", resultSet.getInt("end"));
                record.put("strand", resultSet.getString("strand"));
                int width = resultSet.getInt("end") - resultSet.getInt("start") +1;
                width_list.add((double) width);
            }
        }catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
        //System.out.println("query results is: " + results);
        exitDB();
        return results;
    }
}
