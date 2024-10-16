package sqldbOperate;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author Benchen Ye
 * @create 2024-10--20:28
 * @function get all the genome coordinates for the query key `refname`
 */
public class SQLiteQuery {

    public static List<Map<String, Object>> queryGenomeCoorDatabaseRecord(String query_refname) {
        // can use the chunk to improve the performance
        // DB need the whole path
        String url = "jdbc:sqlite:coordinate_db.db";

        List<Map<String, Object>> results = new ArrayList<>();
        // query database table name is `elementgenomecoordinate`
        String query = "SELECT *  FROM elementgenomecoordinate WHERE refname = ?";
        System.out.println("Starting the query in the Table elementgenomecoordinate!");
        System.out.println("The query keyword is : " + query_refname);
        try (Connection connection = DriverManager.getConnection(url);
            PreparedStatement prepared_statement = connection.prepareStatement(query)) {
            prepared_statement.setString(1, query_refname);
            try (ResultSet resultSet = prepared_statement.executeQuery()) {
                while (resultSet.next()) {
                    Map<String, Object> record = new HashMap<>();
                    // get the items from database table with getString/getInt
                    record.put("chrom", resultSet.getString("chrom"));
                    record.put("start", resultSet.getInt("start"));
                    record.put("end", resultSet.getInt("end"));
                    record.put("strand", resultSet.getString("strand"));
                    results.add(record);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }
}

