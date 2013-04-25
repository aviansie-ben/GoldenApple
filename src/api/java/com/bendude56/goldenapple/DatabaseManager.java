package com.bendude56.goldenapple;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface DatabaseManager {
	boolean usingMySql();
	
	void execute(String command) throws SQLException;
	void execute(String command, Object... parameters) throws SQLException;
	
	ResultSet executeQuery(String command) throws SQLException;
	ResultSet executeQuery(String command, Object... parameters) throws SQLException;
	
	void executeFromResource(String resourceName) throws SQLException, IOException;
	void executeFromResource(String resourceName, Object... parameters) throws SQLException, IOException;
	
	ResultSet executeQueryFromResource(String resourceName) throws SQLException, IOException;
	ResultSet executeQueryFromResource(String resourceName, Object... parameters) throws SQLException, IOException;
	
	ResultSet executeReturnGenKeys(String command, Object... parameters) throws SQLException;
	
	public void closeResult(ResultSet r) throws SQLException;
	public void createOrUpdateTable(String tableName);
	public void close();
}
