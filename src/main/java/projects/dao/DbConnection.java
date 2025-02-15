package projects.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import projects.Exception.DbException;



public class DbConnection {
  private static String SCHEMA = "projects";
  private static String USER = "projects";
  private static String PASSWORD = "projects";
  private static String HOST = "localhost";
  private static final int PORT = 3306;

  public static Connection getConnection() {
    String uri = 
    		String.format("jdbc:mysql://%s:%d/%s?user=%s&password=%s&useSSL=false", HOST, PORT, SCHEMA,
        USER, PASSWORD);
    
    System.out.println("Connection to schema " + SCHEMA + " is successful.=" + uri);
    try {
      Connection conn = DriverManager.getConnection(uri);
      System.out.println("Successfully obtained a connection=" + uri);
      return conn;
    } catch (SQLException e) {
      throw new DbException(e);
		}
	}
}
