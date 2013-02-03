package com.bendude56.goldenapple;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;

import org.sqlite.JDBC;

/**
 * Represents a GoldenApple database connection
 * 
 * @author ben_dude56
 */
public final class Database {
	private Connection	connection;
	private boolean		mySql;
	
	private HashMap<ResultSet, PreparedStatement> toClose = new HashMap<ResultSet, PreparedStatement>();

	protected Database() {
		if (GoldenApple.getInstance().mainConfig.getBoolean("database.useMySQL", false)) {
			mySql = true;
			try {
				Class.forName("com.mysql.jdbc.Driver");
				Connection c = DriverManager.getConnection("jdbc:mysql://" + GoldenApple.getInstance().mainConfig.getString("database.host", "localhost") + "/mysql", GoldenApple.getInstance().mainConfig.getString("database.user", ""), GoldenApple.getInstance().mainConfig.getString("database.password", ""));
				if (!c.isValid(1000)) {
					GoldenApple.log(Level.SEVERE, "Failed to connect to MySQL database!");
					return;
				}
				connection = c;
				execute("CREATE DATABASE IF NOT EXISTS " + GoldenApple.getInstance().mainConfig.getString("database.database", "ga"));
				execute("USE " + GoldenApple.getInstance().mainConfig.getString("database.database", "ga"));
				GoldenApple.log("Successfully connected to MySQL database at \'" + GoldenApple.getInstance().mainConfig.getString("database.host") + "\'");
			} catch (Exception e) {
				GoldenApple.log(Level.SEVERE, "Failed to connect to MySQL database!");
				GoldenApple.log(e);
				return;
			}
		} else {
			mySql = false;
			try {
				GoldenApple.log(Level.WARNING, "SQLite support has not yet been fully added. It is recommended that you use MySQL.");
				Driver d = new JDBC();
				GoldenApple.log("Loading database using SQLite v" + d.getMajorVersion() + "." + d.getMinorVersion());
				connection = d.connect("jdbc:sqlite:" + GoldenApple.getInstance().mainConfig.getString("database.path"), new Properties());
				GoldenApple.log("Successfully connected to SQLite database at \'" + GoldenApple.getInstance().mainConfig.getString("database.path") + "\'");
			} catch (Exception e) {
				GoldenApple.log(Level.SEVERE, "Failed to connect to SQLite database!");
				GoldenApple.log(e);
				return;
			}
		}
	}

	public boolean usingMySql() {
		return mySql;
	}

	/**
	 * Executes an SQL command on the database. This should <strong>not</strong>
	 * be used to execute a command with user data, because their data will not
	 * be sanitized. {@link Database#execute(String command, Object[] parameters)}
	 * should be used instead when user-entered data will be used in the
	 * command.
	 * 
	 * @param command The command to execute
	 */
	public void execute(String command) throws SQLException {
		execute(command, new Object[0]);
	}

	/**
	 * Executes an SQL command on the database
	 * 
	 * @param command The command to execute
	 * @param parameters The arguments that should be added in place of ?s in
	 *            the statement before it is executed
	 */
	public void execute(String command, Object... parameters) throws SQLException {
		PreparedStatement s = connection.prepareStatement(command);
		try {
			for (int i = 0; i < parameters.length; i++) {
				s.setObject(i + 1, parameters[i]);
			}
			s.execute();
		} finally {
			s.close();
		}
	}

	/**
	 * Executes an SQL query on the database and returns the result. This should
	 * <strong>not</strong> be used to execute a command with user data, because
	 * their data will not be sanitized.
	 * {@link Database#executeQuery(String command, Object[] parameters)} should
	 * be used instead when user-entered data will be used in the command.
	 * 
	 * @param command The command to execute
	 */
	public ResultSet executeQuery(String command) throws SQLException {
		return executeQuery(command, new Object[0]);
	}

	/**
	 * Executes an SQL query on the database and returns the result.
	 * 
	 * @param command The command to execute
	 * @param parameters The arguments that should be added in place of ?s in
	 *            the statement before it is executed
	 */
	public ResultSet executeQuery(String command, Object... parameters) throws SQLException {
		PreparedStatement s = connection.prepareStatement(command);
		try {
			for (int i = 0; i < parameters.length; i++) {
				s.setObject(i + 1, parameters[i]);
			}
			
			ResultSet r = s.executeQuery();
			
			toClose.put(r, s);

			return r;
		} finally {
			if (!toClose.containsValue(s))
				s.close();
		}
	}
	
	public void executeFromResource(String resourceName) throws SQLException, IOException {
		executeFromResource(resourceName, new Object[0]);
	}
	
	public void executeFromResource(String resourceName, Object... parameters) throws SQLException, IOException {
		execute(readResource("sql/" + ((mySql) ? "mysql" : "sqlite") + "/" + resourceName + ".sql"), parameters);
	}
	
	public ResultSet executeQueryFromResource(String resourceName) throws SQLException, IOException {
		return executeQueryFromResource(resourceName, new Object[0]);
	}
	
	public ResultSet executeQueryFromResource(String resourceName, Object... parameters) throws SQLException, IOException {
		return executeQuery(readResource("sql/" + ((mySql) ? "mysql" : "sqlite") + "/" + resourceName + ".sql"), parameters);
	}
	
	public ResultSet executeReturnGenKeys(String command) throws SQLException {
		return executeReturnGenKeys(command, new Object[0]);
	}
	
	public ResultSet executeReturnGenKeys(String command, Object... parameters) throws SQLException {
		PreparedStatement s = connection.prepareStatement(command, Statement.RETURN_GENERATED_KEYS);
		try {
			for (int i = 0; i < parameters.length; i++) {
				s.setObject(i + 1, parameters[i]);
			}
			s.execute();
		
			ResultSet r = s.getGeneratedKeys();
			
			toClose.put(r, s);
			
			return r;
		} finally {
			if (!toClose.containsValue(s))
				s.close();
		}
	}
	
	private String readResource(String resource) throws IOException {
		Writer w = new StringWriter();
		char[] buffer = new char[1024];
		InputStream i = null;
		
		try {
            Reader r = new BufferedReader(
                    new InputStreamReader(i = getClass().getClassLoader().getResourceAsStream(resource), "UTF-8"));
            int n;
            while ((n = r.read(buffer)) != -1) {
                w.write(buffer, 0, n);
            }
        } finally {
        	if (i != null)
        		i.close();
        }
		
		return w.toString();
	}
	
	public void closeResult(ResultSet r) throws SQLException {
		toClose.get(r).close();
	}

	/**
	 * Closes the connection to the database
	 */
	public void close() {
		try {
			if (connection == null || connection.isClosed())
				return;
			connection.close();
		} catch (SQLException e) {
			// Ignore any SQL problems while closing the connection
		}
	}
}
