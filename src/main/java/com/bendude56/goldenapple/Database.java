package com.bendude56.goldenapple;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;

import org.sqlite.JDBC;

public final class Database {
	private Connection connection;
	
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
	
	public void execute(String command) throws SQLException {
		execute(command, new Object[0]);
	}
	
	public void execute(String command, Object[] parameters) throws SQLException {
		PreparedStatement s = connection.prepareStatement(command);
		for (int i = 0; i < parameters.length; i++) {
			s.setObject(i, parameters[i]);
		}
		s.execute();
	}
	
	public ResultSet executeQuery(String command) throws SQLException {
		return executeQuery(command, new Object[0]);
	}
	
	public ResultSet executeQuery(String command, Object[] parameters) throws SQLException {
		PreparedStatement s = connection.prepareStatement(command);
		for (int i = 0; i < parameters.length; i++) {
			s.setObject(i, parameters[i]);
		}
		return s.executeQuery();
	}
	
	public void close() {
		try {
			connection.close();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}
