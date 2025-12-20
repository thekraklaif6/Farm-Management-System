
package com.gluonhq;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DB {

    private static final String URL ="jdbc:postgresql://localhost:5432/DB_project";
    private static final String USER = "postgres";
    private static final String PASSWORD = "139963";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
