package com.bendude56.goldenapple.audit;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.lock.audit.LockCreateEntry;
import com.bendude56.goldenapple.lock.audit.LockDeleteEntry;
import com.bendude56.goldenapple.lock.audit.LockMoveEntry;
import com.bendude56.goldenapple.lock.audit.LockOverrideDisableEntry;
import com.bendude56.goldenapple.lock.audit.LockOverrideEnableEntry;
import com.bendude56.goldenapple.lock.audit.LockOverrideEntry;
import com.bendude56.goldenapple.permissions.audit.GroupAddMemberEntry;
import com.bendude56.goldenapple.permissions.audit.GroupAddOwnerEntry;
import com.bendude56.goldenapple.permissions.audit.GroupRemoveMemberEntry;
import com.bendude56.goldenapple.permissions.audit.GroupRemoveOwnerEntry;
import com.bendude56.goldenapple.permissions.audit.ObjectCreateEntry;
import com.bendude56.goldenapple.permissions.audit.ObjectDeleteEntry;
import com.bendude56.goldenapple.permissions.audit.PermissionGrantEntry;
import com.bendude56.goldenapple.permissions.audit.PermissionRevokeEntry;
import com.bendude56.goldenapple.punish.audit.BanEntry;
import com.bendude56.goldenapple.punish.audit.BanVoidEntry;
import com.bendude56.goldenapple.punish.audit.MuteEntry;
import com.bendude56.goldenapple.punish.audit.MuteVoidEntry;

public abstract class AuditEntry {
    private static HashMap<Integer, Class<? extends AuditEntry>> registeredEntries = new HashMap<Integer, Class<? extends AuditEntry>>();
    
    public static void registerAuditEntry(Class<? extends AuditEntry> auditClass) {
        try {
            registerAuditEntry(auditClass.newInstance().entryId, auditClass);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Error in class for audit entry " + auditClass.getSimpleName());
        }
    }
    
    public static void registerAuditEntry(int eventId, Class<? extends AuditEntry> auditClass) {
        if (registeredEntries.containsKey(eventId)) {
            throw new IllegalArgumentException("Audit ID conflict!");
        }
        
        registeredEntries.put(eventId, auditClass);
    }
    
    public static AuditEntry loadEntry(ResultSet main, ResultSet metadata) throws SQLException {
        int entryId = main.getInt("EventID");
        try {
            AuditEntry e = registeredEntries.get(entryId).newInstance();
            e.load(main, metadata);
            return e;
        } catch (InstantiationException | IllegalAccessException | NullPointerException e) {
            throw new RuntimeException("Error in class for audit entry " + registeredEntries.get(entryId).getSimpleName());
        }
    }
    
    static {
        registerAuditEntry(AuditStartEntry.class);
        registerAuditEntry(AuditStopEntry.class);
        registerAuditEntry(ModuleEnableEntry.class);
        registerAuditEntry(ModuleDisableEntry.class);
        
        registerAuditEntry(LockOverrideEnableEntry.class);
        registerAuditEntry(LockOverrideDisableEntry.class);
        registerAuditEntry(LockOverrideEntry.class);
        registerAuditEntry(LockCreateEntry.class);
        registerAuditEntry(LockDeleteEntry.class);
        registerAuditEntry(LockMoveEntry.class);
        
        registerAuditEntry(BanEntry.class);
        registerAuditEntry(MuteEntry.class);
        registerAuditEntry(BanVoidEntry.class);
        registerAuditEntry(MuteVoidEntry.class);
        
        registerAuditEntry(PermissionGrantEntry.class);
        registerAuditEntry(PermissionRevokeEntry.class);
        registerAuditEntry(GroupAddMemberEntry.class);
        registerAuditEntry(GroupRemoveMemberEntry.class);
        registerAuditEntry(ObjectCreateEntry.class);
        registerAuditEntry(ObjectDeleteEntry.class);
        registerAuditEntry(GroupAddOwnerEntry.class);
        registerAuditEntry(GroupRemoveOwnerEntry.class);
    }
    
    public final int entryId;
    public final AuditEntryLevel severity;
    public final String module;
    
    public long auditId = -1;
    public Timestamp logTime = new Timestamp(System.currentTimeMillis());
    
    public AuditEntry(int entryId, AuditEntryLevel severity, String module) {
        this.entryId = entryId;
        this.severity = severity;
        this.module = module;
    }
    
    protected abstract void loadMetadata(Map<String, AuditMetadata> metadata);
    protected abstract Map<String, AuditMetadata> saveMetadata();
    public abstract String formatMessage();
    
    public final void load(ResultSet main, ResultSet metadata) throws SQLException {
        this.auditId = main.getLong("ID");
        this.logTime = main.getTimestamp("Time");
        
        Map<String, AuditMetadata> parsedMetadata = new HashMap<String, AuditMetadata>();
        while (metadata.next()) {
            AuditMetadata m = new AuditMetadata(metadata);
            parsedMetadata.put(m.param, m);
        }
        
        loadMetadata(parsedMetadata);
    }
    
    public final void save() throws SQLException {
        if (auditId == -1) {
            ResultSet r = GoldenApple.getInstanceDatabaseManager().executeReturnGenKeys("INSERT INTO AuditLog (Time, EventID) VALUES (?, ?)", logTime, entryId);
            try {
                if (r.next()) {
                    auditId = r.getLong(1);
                } else {
                    throw new SQLException("Failed to retrieve inserted primary key!");
                }
            } finally {
                GoldenApple.getInstanceDatabaseManager().closeResult(r);
            }
        } else {
            stripMetadata();
        }
        
        Map<String, AuditMetadata> metadata = saveMetadata();
        
        for (Map.Entry<String, AuditMetadata> m : metadata.entrySet()) {
            m.getValue().save();
        }
    }
    
    private void stripMetadata() throws SQLException {
        GoldenApple.getInstanceDatabaseManager().execute("DELETE FROM AuditLogParams WHERE AuditID=?", auditId);
    }
    
    private AuditMetadata createMetadata(String name, Long iValue, String sValue) {
        return new AuditMetadata(auditId, name, iValue, sValue);
    }
    
    protected void appendMetadata(Map<String, AuditMetadata> metadata, String name, Long iValue) {
        metadata.put(name, createMetadata(name, iValue, null));
    }
    
    protected void appendMetadata(Map<String, AuditMetadata> metadata, String name, String sValue) {
        metadata.put(name, createMetadata(name, null, sValue));
    }
    
    protected void appendMetadata(Map<String, AuditMetadata> metadata, String name, Long iValue, String sValue) {
        metadata.put(name, createMetadata(name, iValue, sValue));
    }
    
    @Override
    public String toString() {
        return "[" + new SimpleDateFormat("yyyy-MM-dd hh:mm:ssaa").format(logTime) + "] [" + module + "] " + severity.prefix + " " + formatMessage();
    }
    
    public class AuditMetadata {
        public long auditId;
        public String param;
        
        public Long valueInt;
        public String valueString;
        
        public AuditMetadata(ResultSet r) throws SQLException {
            auditId = r.getLong("AuditID");
            param = r.getString("Param");
            valueInt = r.getLong("ValueInt");
            valueString = r.getString("ValueString");
        }
        
        public AuditMetadata(long auditId, String param, Long valueInt, String valueString) {
            this.auditId = auditId;
            this.param = param;
            this.valueInt = valueInt;
            this.valueString = valueString;
        }
        
        public void save() throws SQLException {
            GoldenApple.getInstanceDatabaseManager().execute("INSERT INTO AuditLogParams (AuditID, Param, ValueInt, ValueString) VALUES (?, ?, ?, ?)", auditId, param, valueInt, valueString);
        }
    }
    
    public enum AuditEntryLevel {
        INFO("[i]", 0), WARNING("[!]", 1), SEVERE("[!!]", 2), EXTREME("[!!!]", 3);
        
        public String prefix;
        public int id;
        
        AuditEntryLevel(String prefix, int id) {
            this.prefix = prefix;
            this.id = id;
        }
        
        public static AuditEntryLevel fromId(int id) {
            for (AuditEntryLevel level : AuditEntryLevel.values()) {
                if (level.id == id) {
                    return level;
                }
            }
            return AuditEntryLevel.EXTREME;
        }
    }
}
