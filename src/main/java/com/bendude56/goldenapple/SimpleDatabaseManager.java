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
import java.util.Map.Entry;
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
    public static int DB_VERSION = 5;
    
    private Connection connection;
    private boolean mySql;
    
    private HashMap<ResultSet, PreparedStatement> toClose = new HashMap<ResultSet, PreparedStatement>();
    private HashMap<PreparedStatement, StackTraceElement> queryStackTraces = new HashMap<PreparedStatement, StackTraceElement>();
    private HashMap<String, HashMap<Integer, AdvancedTableUpdater>> updaters = new HashMap<String, HashMap<Integer, AdvancedTableUpdater>>();
    
    protected SimpleDatabaseManager() {
        mySql = GoldenApple.getInstanceMainConfig().getBoolean("database.useMySQL", false);
        connect();
    }
    
    private void connect() {
        if (mySql) {
            try {
                // Open a JDBC connection to the database server
                Class.forName("com.mysql.jdbc.Driver");
                Connection c = DriverManager.getConnection("jdbc:mysql://" + GoldenApple.getInstanceMainConfig().getString("database.host", "localhost") + "/?allowMultiQueries=true", GoldenApple.getInstanceMainConfig().getString("database.user", ""), GoldenApple.getInstanceMainConfig().getString("database.password", ""));
                
                // Ensure that the connection was successful
                if (!c.isValid(3)) {
                    GoldenApple.log(Level.SEVERE, "Failed to connect to MySQL database!");
                    return;
                }
                connection = c;
                
                // Create the database if it doesn't already exist
                if (!GoldenApple.getInstanceMainConfig().getBoolean("database.doNotCreate", false)) {
                    execute("CREATE DATABASE IF NOT EXISTS " + GoldenApple.getInstanceMainConfig().getString("database.database", "ga"));
                }
                
                // Select the database for use
                execute("USE " + GoldenApple.getInstanceMainConfig().getString("database.database", "ga"));
                GoldenApple.log("Successfully connected to MySQL database at \'" + GoldenApple.getInstanceMainConfig().getString("database.host") + "\'");
            } catch (Exception e) {
                GoldenApple.log(Level.SEVERE, "Failed to connect to MySQL database!");
                GoldenApple.log(e);
                return;
            }
        } else {
            try {
                GoldenApple.log(Level.WARNING, "SQLite support has not yet been fully added. It is recommended that you use MySQL.");
                Driver d = new JDBC();
                GoldenApple.log("Loading database using SQLite v" + d.getMajorVersion() + "." + d.getMinorVersion());
                connection = d.connect("jdbc:sqlite:" + GoldenApple.getInstanceMainConfig().getString("database.path"), new Properties());
                execute("PRAGMA foreign_keys = ON");
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
    
    @Override
    public void execute(String command) throws SQLException {
        execute(command, new Object[0]);
    }
    
    @Override
    public void execute(String command, Object... parameters) throws SQLException {
        if (mySql && !connection.isValid(3)) {
            close();
            connect();
        }
        
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
    
    @Override
    public ResultSet executeQuery(String command) throws SQLException {
        return executeQuery(command, new Object[0]);
    }
    
    @Override
    public ResultSet executeQuery(String command, Object... parameters) throws SQLException {
        if (mySql && !connection.isValid(3)) {
            close();
            connect();
        }
        
        PreparedStatement s = connection.prepareStatement(command);
        try {
            for (int i = 0; i < parameters.length; i++) {
                s.setObject(i + 1, parameters[i]);
            }
            
            ResultSet r = s.executeQuery();
            
            toClose.put(r, s);
            queryStackTraces.put(s, new Exception().getStackTrace()[1]);
            
            return r;
        } finally {
            if (!toClose.containsValue(s)) {
                s.close();
            }
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
        if (mySql && !connection.isValid(3)) {
            close();
            connect();
        }
        
        PreparedStatement s = connection.prepareStatement(command, Statement.RETURN_GENERATED_KEYS);
        try {
            for (int i = 0; i < parameters.length; i++) {
                s.setObject(i + 1, parameters[i]);
            }
            s.execute();
            
            ResultSet r = s.getGeneratedKeys();
            
            toClose.put(r, s);
            queryStackTraces.put(s, new Exception().getStackTrace()[1]);
            
            return r;
        } finally {
            if (!toClose.containsValue(s)) {
                s.close();
            }
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
            if (i != null) {
                i.close();
            }
        }
        
        return w.toString();
    }
    
    private boolean resourceExists(String resource) throws IOException {
        return getClass().getClassLoader().getResource(resource) != null;
    }
    
    @Override
    public void closeResult(ResultSet r) throws SQLException {
        queryStackTraces.remove(toClose.get(r));
        toClose.remove(r).close();
    }
    
    @Override
    public void registerTableUpdater(String table, int version, AdvancedTableUpdater updater) {
        table = table.toLowerCase();
        
        if (!updaters.containsKey(table)) {
            updaters.put(table, new HashMap<Integer, AdvancedTableUpdater>());
        }
        if (!updaters.get(table).containsKey(version)) {
            updaters.get(table).put(version, updater);
        }
    }
    
    @Override
    public boolean createOrUpdateTable(String tableName) {
        int expectedDbVersion = DB_VERSION;
        try {
            tableName = tableName.toLowerCase();
            int actualDbVersion = GoldenApple.getInstance().getDatabaseVersionConfig().getInt("tableVersions." + tableName, 0);
            
            if (actualDbVersion == 0) {
                // The table doesn't yet exist. Execute the creation query
                executeFromResource(tableName + "_create");
                
                // Change the version id
                GoldenApple.getInstance().getDatabaseVersionConfig().set("tableVersions." + tableName, expectedDbVersion);
                ((YamlConfiguration) GoldenApple.getInstance().getDatabaseVersionConfig()).save(new File(GoldenApple.getInstance().getDataFolder() + "/dbversion.yml"));
                GoldenApple.log("Table " + tableName + " has been created at database version " + expectedDbVersion + "...");
                
                return true;
            } else if (actualDbVersion < expectedDbVersion) {
                boolean executed = false;
                
                // The table is a version less than expected. Update it
                // sequentially until it is up-to-date
                for (int i = actualDbVersion + 1; i <= expectedDbVersion; i++) {
                    if (resourceExists("sql/" + ((mySql) ? "mysql" : "sqlite") + "/update/" + i + "/" + tableName + "_update.sql")) {
                        executeFromResource("update/" + i + "/" + tableName + "_update");
                        executed = true;
                    }
                    
                    if (updaters.containsKey(tableName) && updaters.get(tableName).containsKey(i)) {
                        if (mySql) {
                            updaters.get(tableName).get(i).doMySqlUpdate(this);
                        } else {
                            updaters.get(tableName).get(i).doSqliteUpdate(this);
                        }
                    }
                }
                
                // Change the version id
                GoldenApple.getInstance().getDatabaseVersionConfig().set("tableVersions." + tableName, expectedDbVersion);
                ((YamlConfiguration) GoldenApple.getInstance().getDatabaseVersionConfig()).save(new File(GoldenApple.getInstance().getDataFolder() + "/dbversion.yml"));
                if (executed) {
                    GoldenApple.log("Table " + tableName + " has been updated from version " + actualDbVersion + " to version " + expectedDbVersion + "...");
                }
                
                return true;
            } else if (actualDbVersion > expectedDbVersion) {
                GoldenApple.log(Level.SEVERE, "Table " + tableName + " has a newer database revision than this version of GoldenApple and cannot be used.");
                return false;
            } else {
                // The table is up to date
                return true;
            }
        } catch (Throwable e) {
            GoldenApple.log(Level.SEVERE, "Failed to create or update table " + tableName + ":");
            GoldenApple.log(Level.SEVERE, e);
            return false;
        }
    }
    
    @Override
    public boolean isConnected() {
        try {
            return connection != null && (!mySql || connection.isValid(3));
        } catch (SQLException e) {
            return false;
        }
    }
    
    @Override
    public void close() {
        if (toClose.size() > 0) {
            HashMap<String, Integer> leakyQueries = new HashMap<String, Integer>();
            
            for (PreparedStatement s : toClose.values()) {
                String leakPoint = queryStackTraces.get(s).toString();
                
                if (leakyQueries.containsKey(leakPoint)) {
                    leakyQueries.put(leakPoint, leakyQueries.get(leakPoint) + 1);
                } else {
                    leakyQueries.put(leakPoint, 1);
                }
            }
            
            GoldenApple.log(Level.WARNING, "ResultSet leakage has been detected! " + toClose.size() + " ResultSets have leaked! Please report the following leaks:");
            
            for (Entry<String, Integer> leak : leakyQueries.entrySet()) {
                GoldenApple.log(Level.WARNING, "  " + leak.getKey() + " has leaked " + leak.getValue() + " ResultSets!");
            }
        }
        
        try {
            if (connection == null || connection.isClosed()) {
                return;
            }
            connection.close();
        } catch (SQLException e) {
            // Ignore any SQL problems while closing the connection
        }
    }
}
