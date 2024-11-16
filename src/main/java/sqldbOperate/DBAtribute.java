package sqldbOperate;

import org.sqlite.core.DB;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Benchen Ye
 * @create 2024-11--22:35
 */
public class DBAtribute {
    // DB need the whole path
    public static final String DATABASE_URL = "jdbc:sqlite:coordinate_db.db";
    public static Connection connection = null;
    public static Statement statement = null;

    public static Statement initialDB() {
        try {
            connection = DriverManager.getConnection(DATABASE_URL);
            statement = connection.createStatement();
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            System.exit(1);
        }
        System.out.println("successfully connected to database!");
        System.out.println("---------");
        return statement;
    }

    public static void exitDB() {
            try {
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                System.out.println("Stop database error: " + e.getMessage());
                System.exit(0);
            }
        System.out.println("---------");
        System.out.println("Database exit successfully.");
    }

}
