package com.bendude56.goldenapple.permissions;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Level;

import com.bendude56.goldenapple.DatabaseManager;
import com.bendude56.goldenapple.DatabaseManager.AdvancedTableUpdater;
import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.util.UUIDFetcher;

public class UserUuidTableUpdater implements AdvancedTableUpdater {
    
    @Override
    public void doMySqlUpdate(DatabaseManager manager) throws SQLException {
        GoldenApple.log(Level.WARNING, "Old database schema without UUID support detected! Updating...");
        
        try {
            manager.execute("ALTER TABLE Users ADD UUID VARCHAR(36) CHARACTER SET ASCII COLLATE ascii_general_ci NOT NULL AFTER Name");
        } catch (SQLException e) {
            GoldenApple.log(Level.WARNING, "Failed to add UUID column! This may be the result of a previous half-completed upgrade. Will ignore, but errors may result...");
        }
        
        ArrayList<String> names = new ArrayList<String>();
        ResultSet r = manager.executeQuery("SELECT Name FROM Users");
        
        try {
            while (r.next()) {
                names.add(r.getString("Name"));
            }
        } finally {
            manager.closeResult(r);
        }
        
        try {
            Map<String, UUID> uuids = new UUIDFetcher(names).call();
            
            for (Entry<String, UUID> e : uuids.entrySet()) {
                manager.execute("UPDATE Users SET UUID=? WHERE Name=?", e.getValue().toString(), e.getKey());
            }
            
            boolean die = false;
            for (String name : names) {
                if (!uuids.containsKey(name)) {
                    die = true;
                    GoldenApple.log(Level.SEVERE, "Failed to look up UUID for " + name);
                }
            }
            
            if (die) {
                throw new RuntimeException("UUID lookups for some users failed! These users must be manually updated or removed from the database!");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to look up UUIDs! Table schema may be damaged!", e);
        }
        
        try {
            manager.execute("ALTER TABLE Users ADD UNIQUE(UUID)");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add unique UUID index!", e);
        }
    }
    
    @Override
    public void doSqliteUpdate(DatabaseManager manager) throws SQLException {
        throw new UnsupportedOperationException();
    }
    
}
