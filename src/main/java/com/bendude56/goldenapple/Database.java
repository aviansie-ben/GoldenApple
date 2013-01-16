package com.bendude56.goldenapple;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
		for (int i = 0; i < parameters.length; i++) {
			s.setObject(i + 1, parameters[i]);
		}
		s.execute();
	}

	/**
	 * Executes an SQL query on the database and returns the result. This should
	 * <strong>not</strong> be used to execute a command with user data, because
	 * their data will not be sanitized.
	 * {@link Database#executeQuery(String comman, Object[] parameters)} should
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
		for (int i = 0; i < parameters.length; i++) {
			s.setObject(i + 1, parameters[i]);
		}
		return s.executeQuery();
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
