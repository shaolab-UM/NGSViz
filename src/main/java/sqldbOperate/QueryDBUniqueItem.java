package sqldbOperate;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Benchen Ye
 * @create 2024-11--17:20
 * @function link to the database and get unique query item including in the database
 * @input String `query_key` to specify the query item
 * @output List<String> to save the unique value for query item
 */
public class QueryDBUniqueItem extends DBAtribute{
    public static void main(String[] args) {
        String query_key = "DB";
        List<String> query_res = QueryDBUniqueItem.getDBQueryUniqueItem(query_key);
        System.out.println("query_res : " + query_res);
    }
    public static List<String> getDBQueryUniqueItem(String query_key) {
        List<String> query_results = new ArrayList<>();
        ResultSet resultSet = null;
        try {
            statement = initialDB();
            String query = "SELECT DISTINCT " + query_key + " FROM defaultTbl";
            resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                String query_res = resultSet.getString(query_key);
                query_results.add(query_res);
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
        exitDB();
        return query_results;
    }
}
