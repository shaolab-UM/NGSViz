package com.NGSVir.sqldbOperate;

import com.NGSVir.configSet.InputParameterAttributes;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.*;

/**
 * @author Benchen Ye
 * @create 2024-11--22:35
 */
public class DBAtribute extends InputParameterAttributes {
    // DB need the whole path
    //public static final String DATABASE_URL = "jdbc:sqlite:coordinate_db.db";
    //public static String dbFilePath = DBAtribute.class.getResource(db_path).getFile();
    //public static String dbFilePath = "/Users/bencheye/myProj/ngsPlot/NGSVir2/src/main/resources/genomeCoordinate.db";
    public static final String DATABASE_URL = "jdbc:sqlite::resource:genomeCoordinate.db";
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
    }

}
