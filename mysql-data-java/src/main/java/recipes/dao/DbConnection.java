package recipes.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import recipes.exception.DbException;

public class DbConnection {
	private static String HOST = "localhost";
	  private static String PASSWORD = "recipes";
	  private static int PORT = 3306;
	  private static String SCHEMA = "recipes";
	  private static String USER = "recipes";

	  public static Connection getConnection() {
	    String uri = String.format("jdbc:mysql://%s:%d/%s?user=%s&password=%s", HOST, PORT, SCHEMA,
	        USER, PASSWORD);
	   // System.out.println("Connecting with uri= " + uri);
	    try {
	      Connection conn = DriverManager.getConnection(uri);
	      System.out.println("Congratualations you are connected to'" + SCHEMA + "' successful.");
	      return conn;
	    } catch (SQLException e) {
	      System.out.println("Unable to get connection at " + uri);
	      throw new DbException("Unable to get connection at \" + uri");
	    }
	  }
}
