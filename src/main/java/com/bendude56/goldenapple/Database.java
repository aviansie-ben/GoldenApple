package com.bendude56.goldenapple;

import java.sql.Connection;
import java.sql.Driver;
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

	protected Database() {
		try {
			Driver d = new JDBC();
			GoldenApple.log("Loading database using SQLite v" + d.getMajorVersion() + "." + d.getMinorVersion());
			connection = d.connect("jdbc:sqlite:" + GoldenApple.getInstance().mainConfig.getString("database.path"), new Properties());
			GoldenApple.log("Successfully connected to database at \'" + GoldenApple.getInstance().mainConfig.getString("database.path") + "\'");
		} catch (Exception e) {
			GoldenApple.log(Level.SEVERE, "Failed to connect to SQLite database!");
			GoldenApple.log(e);
			return;
		}
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
			connection.close();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}
