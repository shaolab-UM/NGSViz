package sqldbOperate;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Benchen Ye
 * @create 2024-11--14:05
 */
public class QueryDefaultDB extends DBAtribute{
    public static void main(String[] args) {
        String query_key = "DefaultDB";
        String query_res = getDefaultDBValue(query_key, "hg19", "tss");
        System.out.println("query_res : " + query_res);
    }

    public static String getDefaultDBValue(String query_item, String genome, String region2plot){
        String query_res = null;
        ResultSet resultSet = null;
        try {
            statement = initialDB();
            String query = String.format("SELECT %s FROM defaulttbl WHERE Genome = '%s' AND Region = '%s'", query_item, genome, region2plot);
            System.out.println("The query keyword is : " + query);
            resultSet = statement.executeQuery(query);
            if (resultSet.next()) {
                query_res = resultSet.getString(query_item);
            }
        } catch (SQLException e) {
            System.out.println("Database read data error: " + e.getMessage());
        }
        exitDB();
        return query_res;
    }
}
