package com.bendude56.goldenapple;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface DatabaseManager {
    
    /**
     * Determines whether the database connection is using MySQL or SQLite. Will
     * return true if MySQL is being used or false if SQLite is being used.
     */
    boolean usingMySql();
    
    /**
     * Executes an SQL command on the database. This should <strong>not</strong>
     * be used to execute a command with user data, because their data will not
     * be sanitized.
     * {@link SimpleDatabaseManager#execute(String command, Object[] parameters)}
     * should be used instead when user-entered data will be used in the
     * command.
     * 
     * @param command The command to execute
     */
    void execute(String command) throws SQLException;
    
    /**
     * Executes an SQL command on the database
     * 
     * @param command The command to execute
     * @param parameters The arguments that should be added in place of ?s in
     * the statement before it is executed
     */
    void execute(String command, Object... parameters) throws SQLException;
    
    /**
     * Executes an SQL query on the database and returns the result. This should
     * <strong>not</strong> be used to execute a command with user data, because
     * their data will not be sanitized.
     * {@link SimpleDatabaseManager#executeQuery(String command, Object[] parameters)}
     * should be used instead when user-entered data will be used in the
     * command.
     * 
     * @param command The command to execute
     */
    ResultSet executeQuery(String command) throws SQLException;
    
    /**
     * Executes an SQL query on the database and returns the result.
     * 
     * @param command The command to execute
     * @param parameters The arguments that should be added in place of ?s in
     * the statement before it is executed
     */
    ResultSet executeQuery(String command, Object... parameters) throws SQLException;
    
    /**
     * Executes the requested resource from the sql directory in the resources
     * folder. The resource will be retrieved from either the "mysql" or
     * "sqlite" subdirectory based on which type of database this manager is
     * connected to.
     * 
     * @param resourceName The resource which should be loaded and executed. The
     * ".sql" extension will be applied automatically.
     */
    void executeFromResource(String resourceName) throws SQLException, IOException;
    
    /**
     * Executes the requested resource from the sql directory in the resources
     * folder. The resource will be retrieved from either the "mysql" or
     * "sqlite" subdirectory based on which type of database this manager is
     * connected to.
     * 
     * @param resourceName The resource which should be loaded and executed. The
     * ".sql" extension will be applied automatically.
     * @param parameters The arguments that should be added in place of ?s in
     * the statement before it is executed
     */
    void executeFromResource(String resourceName, Object... parameters) throws SQLException, IOException;
    
    /**
     * Executes the requested resource from the sql directory in the resources
     * folder and returns the results of the query. The resource will be
     * retrieved from either the "mysql" or "sqlite" subdirectory based on which
     * type of database this manager is connected to.
     * 
     * @param resourceName The resource which should be loaded and executed. The
     * ".sql" extension will be applied automatically.
     */
    ResultSet executeQueryFromResource(String resourceName) throws SQLException, IOException;
    
    /**
     * Executes the requested resource from the sql directory in the resources
     * folder and returns the results of the query. The resource will be
     * retrieved from either the "mysql" or "sqlite" subdirectory based on which
     * type of database this manager is connected to.
     * 
     * @param resourceName The resource which should be loaded and executed. The
     * ".sql" extension will be applied automatically.
     * @param parameters The arguments that should be added in place of ?s in
     * the statement before it is executed
     */
    ResultSet executeQueryFromResource(String resourceName, Object... parameters) throws SQLException, IOException;
    
    /**
     * Executes an SQL query on the database and returns any keys which have
     * been generated for any autoincrementing values.
     * 
     * @param command The command to execute
     * @param parameters The arguments that should be added in place of ?s in
     * the statement before it is executed
     */
    ResultSet executeReturnGenKeys(String command, Object... parameters) throws SQLException;
    
    /**
     * Closes the {@link ResultSet} and any associated objects in order to free
     * up used memory.
     * 
     * @param r The set that should be closed
     */
    public void closeResult(ResultSet r) throws SQLException;
    
    public void registerTableUpdater(String table, int version, AdvancedTableUpdater updater);
    
    /**
     * Ensures that the listed table is at the proper revision for this version
     * of GoldenApple. If the table doesn't exist, it will be created using the
     * "&lt;table&gt;_create" resource. If it is out of date, it will be updated
     * by using the "update/&lt;revision&gt;/&lt;table&gt;_update" resources
     * until the table is up-to-date.
     * 
     * @param tableName The name of the table that should be checked.
     * @return True if the table is up-to-date or has been brought up-to-date.
     * False if the table failed to update or the table is at a higher revision
     * than GoldenApple.
     */
    public boolean createOrUpdateTable(String tableName);
    
    /**
     * Checks that the database is successfully connected and that the
     * connection is valid.
     * 
     * @return True if the database can be reached, false otherwise.
     */
    boolean isConnected();
    
    /**
     * Closes the connection to the database
     */
    public void close();
    
    public interface AdvancedTableUpdater {
        public void doMySqlUpdate(DatabaseManager manager) throws SQLException;
        public void doSqliteUpdate(DatabaseManager manager) throws SQLException;
    }
}
