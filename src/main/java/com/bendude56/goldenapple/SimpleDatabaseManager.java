package com.bendude56.goldenapple;

import java.io.BufferedReader;
import java.io.File;
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

import org.bukkit.configuration.file.YamlConfiguration;
import org.sqlite.JDBC;

/**
 * Represents a GoldenApple database connection
 * 
 * @author ben_dude56
 */
public final class SimpleDatabaseManager implements DatabaseManager {
	public static int DB_VERSION = 3;
	
	private Connection	connection;
	private boolean		mySql;
	
	private HashMap<ResultSet, PreparedStatement> toClose = new HashMap<ResultSet, PreparedStatement>();

	protected SimpleDatabaseManager() {
		if (GoldenApple.getInstanceMainConfig().getBoolean("database.useMySQL", false)) {
			mySql = true;
			try {
				// Open a JDBC connection to the database server
				Class.forName("com.mysql.jdbc.Driver");
				Connection c = DriverManager.getConnection("jdbc:mysql://" + GoldenApple.getInstanceMainConfig().getString("database.host", "localhost") + "/?allowMultiQueries=true", GoldenApple.getInstanceMainConfig().getString("database.user", ""), GoldenApple.getInstanceMainConfig().getString("database.password", ""));
				
				// Ensure that the connection was successful
				if (!c.isValid(1000)) {
					GoldenApple.log(Level.SEVERE, "Failed to connect to MySQL database!");
					return;
				}
				connection = c;
				
				// Create the database if it doesn't already exist
				if (!GoldenApple.getInstanceMainConfig().getBoolean("database.doNotCreate", false))
					execute("CREATE DATABASE IF NOT EXISTS " + GoldenApple.getInstanceMainConfig().getString("database.database", "ga"));
				
				// Select the database for use
				execute("USE " + GoldenApple.getInstanceMainConfig().getString("database.database", "ga"));
				GoldenApple.log("Successfully connected to MySQL database at \'" + GoldenApple.getInstanceMainConfig().getString("database.host") + "\'");
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
				connection = d.connect("jdbc:sqlite:" + GoldenApple.getInstanceMainConfig().getString("database.path"), new Properties());
				GoldenApple.log("Successfully connected to SQLite database at \'" + GoldenApple.getInstanceMainConfig().getString("database.path") + "\'");
			} catch (Exception e) {
				GoldenApple.log(Level.SEVERE, "Failed to connect to SQLite database!");
				GoldenApple.log(e);
				return;
			}
		}
	}

	@Override
	public boolean usingMySql() {
		return mySql;
	}

	/**
	 * Executes an SQL command on the database. This should <strong>not</strong>
	 * be used to execute a command with user data, because their data will not
	 * be sanitized. {@link SimpleDatabaseManager#execute(String command, Object[] parameters)}
	 * should be used instead when user-entered data will be used in the
	 * command.
	 * 
	 * @param command The command to execute
	 */
	@Override
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
	@Override
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
	 * {@link SimpleDatabaseManager#executeQuery(String command, Object[] parameters)} should
	 * be used instead when user-entered data will be used in the command.
	 * 
	 * @param command The command to execute
	 */
	@Override
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
	@Override
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
	
	@Override
	public void executeFromResource(String resourceName) throws SQLException, IOException {
		executeFromResource(resourceName, new Object[0]);
	}
	
	@Override
	public void executeFromResource(String resourceName, Object... parameters) throws SQLException, IOException {
		execute(readResource("sql/" + ((mySql) ? "mysql" : "sqlite") + "/" + resourceName + ".sql"), parameters);
	}
	
	@Override
	public ResultSet executeQueryFromResource(String resourceName) throws SQLException, IOException {
		return executeQueryFromResource(resourceName, new Object[0]);
	}
	
	@Override
	public ResultSet executeQueryFromResource(String resourceName, Object... parameters) throws SQLException, IOException {
		return executeQuery(readResource("sql/" + ((mySql) ? "mysql" : "sqlite") + "/" + resourceName + ".sql"), parameters);
	}
	
	public ResultSet executeReturnGenKeys(String command) throws SQLException {
		return executeReturnGenKeys(command, new Object[0]);
	}
	
	@Override
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
	
	private boolean resourceExists(String resource) throws IOException {
		return getClass().getClassLoader().getResource(resource) != null;
	}
	
	@Override
	public void closeResult(ResultSet r) throws SQLException {
		toClose.get(r).close();
		toClose.remove(r);
	}
	
	@Override
	public void createOrUpdateTable(String tableName) {
		int expectedDbVersion = DB_VERSION;
		try {
			tableName = tableName.toLowerCase();
			int actualDbVersion = GoldenApple.getInstance().getDatabaseVersionConfig().getInt("tableVersions." + tableName, 0);
			
			if (actualDbVersion == 0) {
				// The table doesn't yet exist. Execute the creation query
				executeFromResource(tableName + "_create");
				
				// Change the version id
				GoldenApple.getInstance().getDatabaseVersionConfig().set("tableVersions." + tableName, expectedDbVersion);
				((YamlConfiguration)GoldenApple.getInstance().getDatabaseVersionConfig()).save(new File(GoldenApple.getInstance().getDataFolder() + "/dbversion.yml"));
				GoldenApple.log("Table " + tableName + " has been created at database version " + expectedDbVersion + "...");
			} else if (actualDbVersion < expectedDbVersion) {
				boolean executed = false;
				
				// The table is a version less than expected. Update it sequentially until it is up-to-date
				for (int i = actualDbVersion + 1; i <= expectedDbVersion; i++) {
					if (resourceExists("sql/" + ((mySql) ? "mysql" : "sqlite") + "/update/" + i + "/" + tableName + "_update.sql")) {
						executeFromResource("update/" + i + "/" + tableName + "_update");
						executed = true;
					}
				}
				
				// Change the version id
				GoldenApple.getInstance().getDatabaseVersionConfig().set("tableVersions." + tableName, expectedDbVersion);
				((YamlConfiguration)GoldenApple.getInstance().getDatabaseVersionConfig()).save(new File(GoldenApple.getInstance().getDataFolder() + "/dbversion.yml"));
				if (executed)
					GoldenApple.log("Table " + tableName + " has been updated from version " + actualDbVersion + " to version " + expectedDbVersion + "...");
			} else if (actualDbVersion > expectedDbVersion) {
				GoldenApple.log(Level.SEVERE, "Table " + tableName + " has a newer database revision than this version of GoldenApple. Unexpected behaviour may result...");
			}
		} catch (Throwable e) {
			GoldenApple.log(Level.SEVERE, "Failed to create or update table " + tableName + ". More errors might occur as a result.");
			GoldenApple.log(Level.SEVERE, e);
		}
	}

	/**
	 * Closes the connection to the database
	 */
	@Override
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
