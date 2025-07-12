package com.NGSViz.sqldbOperate;

import com.NGSViz.configSet.InputParameterAttributes;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.*;

/**
 * @author Benchen Ye
 * @create 2024-11--22:35
 */
public class DBAtribute extends InputParameterAttributes {
    // DB need the whole path
    public static final String DATABASE_URL = "jdbc:sqlite:"+ db_path;
    public static Connection connection = null;
    public static Statement statement = null;

    public static Statement initialDB() {
        //System.out.println("--- Link the DB path is: " + DATABASE_URL);
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
