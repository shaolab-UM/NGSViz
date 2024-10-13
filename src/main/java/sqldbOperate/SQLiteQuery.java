package sqldbOperate;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Benchen Ye
 * @create 2024-10--20:28
 */
public class SQLiteQuery {
    public static void main(String[] args) {
        List<Map<String, Object>> results = queryDatabaseRecord();
        results.parallelStream().forEach(SQLiteQuery::processRecord);
    }

    public static List<Map<String, Object>> queryDatabaseRecord() {
        String url = "jdbc:sqlite:coordinate_db.db";
        String query_keyword = "hg19.ensembl.genebody.protein_coding";

        List<Map<String, Object>> results = new ArrayList<>();
        String query = "SELECT *  FROM elementgenomecoordinate WHERE refname = ?";
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, query_keyword);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
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
    private static void processRecord(Map<String, Object> record) {
        System.out.println("Processing record: " + record);
    }
}

