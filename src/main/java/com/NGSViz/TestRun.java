package com.NGSViz;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import com.NGSViz.sqldbOperate.ParallelGeneDbProcessor;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.*;

public class TestRun {

    public static String db_path = "/Users/benche/myProj/ngsPlot/NGSViz/database/genomeCoordinate.db";
    public static final String DATABASE_URL = "jdbc:sqlite:" + db_path;
    public static Connection connection = null;
    public static Statement statement = null;
    public static String tbl_name = "hg19_RefSeq";
    public static String biotype = "protein_coding";
    public static String type = "transcript";

    /*public static void main(String[] args) {
        // 2. 实例化 GeneDbProcessor 并调用其处理方法
        ParallelGeneDbProcessor processor = new ParallelGeneDbProcessor(8);

        ParallelGeneDbProcessor.processGeneData(tbl_name, biotype, type);

    }*/




}
