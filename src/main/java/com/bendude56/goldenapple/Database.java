package com.bendude56.goldenapple;

import java.sql.Connection;
import java.sql.Driver;
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
	
	public void execute(String command) {
		try {
			connection.prepareStatement(command).execute();
		} catch (SQLException e) {
			GoldenApple.log(Level.SEVERE, "Failed to perform SQL command: " + command + "!");
			GoldenApple.log(e);
		}
	}
	
	public ResultSet executeQuery(String command) {
		try {
			return connection.prepareStatement(command).executeQuery();
		} catch (SQLException e) {
			GoldenApple.log(Level.SEVERE, "Failed to perform SQL command: " + command + "!");
			GoldenApple.log(e);
			return null;
		}
	}
	
	public void close() {
		try {
			connection.close();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}
