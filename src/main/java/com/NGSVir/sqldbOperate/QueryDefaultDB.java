package com.NGSVir.sqldbOperate;

import java.sql.*;

/**
 * @author Benchen Ye
 * @create 2024-11--14:05
 */
public class QueryDefaultDB extends DBAtribute{
    public static String getDefaultDBValue(String query_item, String genome, String DB, String region2plot){
        String query_res = null;
        ResultSet resultSet;
        try {
            statement = initialDB();
            String query = String.format("SELECT %s FROM defaultTbl WHERE Genome = '%s' AND DB = '%s' AND Region = '%s'",
                    query_item, genome, DB, region2plot);
            System.out.println("The query keyword is : " + query);
            resultSet = statement.executeQuery(query);
            if (resultSet.next()) {
                query_res = resultSet.getString(query_item);
                System.out.println(query_res);
            }
        } catch (SQLException e) {
            System.out.println("Database read data error: " + e.getMessage());
        }
        exitDB();
        return query_res;
    }

}
