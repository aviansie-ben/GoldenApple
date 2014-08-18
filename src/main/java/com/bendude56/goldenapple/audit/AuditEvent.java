package com.bendude56.goldenapple.audit;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.lock.audit.LockCreateEvent;
import com.bendude56.goldenapple.lock.audit.LockDeleteEvent;
import com.bendude56.goldenapple.lock.audit.LockMoveEvent;
import com.bendude56.goldenapple.lock.audit.LockOverrideDisableEvent;
import com.bendude56.goldenapple.lock.audit.LockOverrideEnableEvent;
import com.bendude56.goldenapple.lock.audit.LockOverrideEvent;
import com.bendude56.goldenapple.permissions.audit.GroupAddMemberEvent;
import com.bendude56.goldenapple.permissions.audit.GroupAddOwnerEvent;
import com.bendude56.goldenapple.permissions.audit.GroupRemoveMemberEvent;
import com.bendude56.goldenapple.permissions.audit.GroupRemoveOwnerEvent;
import com.bendude56.goldenapple.permissions.audit.ObjectCreateEvent;
import com.bendude56.goldenapple.permissions.audit.ObjectDeleteEvent;
import com.bendude56.goldenapple.permissions.audit.PermissionGrantEvent;
import com.bendude56.goldenapple.permissions.audit.PermissionRevokeEvent;
import com.bendude56.goldenapple.punish.audit.BanEvent;
import com.bendude56.goldenapple.punish.audit.BanVoidEvent;
import com.bendude56.goldenapple.punish.audit.MuteEvent;
import com.bendude56.goldenapple.punish.audit.MuteVoidEvent;

public abstract class AuditEvent {
    private static HashMap<Integer, Class<? extends AuditEvent>> registeredEvents = new HashMap<Integer, Class<? extends AuditEvent>>();
    
    public static void registerAuditEvent(Class<? extends AuditEvent> auditClass) {
        try {
            registerAuditEvent(auditClass.newInstance().eventId, auditClass);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Error in class for audit event " + auditClass.getSimpleName());
        }
    }
    
    public static void registerAuditEvent(int eventId, Class<? extends AuditEvent> auditClass) {
        if (registeredEvents.containsKey(eventId)) {
            throw new IllegalArgumentException("Audit ID conflict!");
        }
        
        registeredEvents.put(eventId, auditClass);
    }
    
    public static AuditEvent loadEvent(ResultSet main, ResultSet metadata) throws SQLException {
        int eventId = main.getInt("EventID");
        try {
            AuditEvent e = registeredEvents.get(eventId).newInstance();
            e.load(main, metadata);
            return e;
        } catch (InstantiationException | IllegalAccessException | NullPointerException e) {
            throw new RuntimeException("Error in class for audit event " + registeredEvents.get(eventId).getSimpleName());
        }
    }
    
    static {
        registerAuditEvent(AuditStartEvent.class);
        registerAuditEvent(AuditStopEvent.class);
        registerAuditEvent(ModuleEnableEvent.class);
        registerAuditEvent(ModuleDisableEvent.class);
        
        registerAuditEvent(LockOverrideEnableEvent.class);
        registerAuditEvent(LockOverrideDisableEvent.class);
        registerAuditEvent(LockOverrideEvent.class);
        registerAuditEvent(LockCreateEvent.class);
        registerAuditEvent(LockDeleteEvent.class);
        registerAuditEvent(LockMoveEvent.class);
        
        registerAuditEvent(BanEvent.class);
        registerAuditEvent(MuteEvent.class);
        registerAuditEvent(BanVoidEvent.class);
        registerAuditEvent(MuteVoidEvent.class);
        
        registerAuditEvent(PermissionGrantEvent.class);
        registerAuditEvent(PermissionRevokeEvent.class);
        registerAuditEvent(GroupAddMemberEvent.class);
        registerAuditEvent(GroupRemoveMemberEvent.class);
        registerAuditEvent(ObjectCreateEvent.class);
        registerAuditEvent(ObjectDeleteEvent.class);
        registerAuditEvent(GroupAddOwnerEvent.class);
        registerAuditEvent(GroupRemoveOwnerEvent.class);
    }
    
    public final int eventId;
    public final AuditEventLevel severity;
    public final String module;
    
    public long auditId = -1;
    public Timestamp logTime = new Timestamp(System.currentTimeMillis());
    
    public AuditEvent(int eventId, AuditEventLevel severity, String module) {
        this.eventId = eventId;
        this.severity = severity;
        this.module = module;
    }
    
    protected abstract void loadMetadata(HashMap<String, AuditMetadata> metadata);
    protected abstract HashMap<String, AuditMetadata> saveMetadata();
    public abstract String formatMessage();
    
    public final void load(ResultSet main, ResultSet metadata) throws SQLException {
        this.auditId = main.getLong("ID");
        this.logTime = main.getTimestamp("Time");
        
        HashMap<String, AuditMetadata> parsedMetadata = new HashMap<String, AuditMetadata>();
        while (metadata.next()) {
            AuditMetadata m = new AuditMetadata(metadata);
            parsedMetadata.put(m.param, m);
        }
        
        loadMetadata(parsedMetadata);
    }
    
    public final void save() throws SQLException {
        if (auditId == -1) {
            ResultSet r = GoldenApple.getInstanceDatabaseManager().executeReturnGenKeys("INSERT INTO AuditLog (Time, EventID) VALUES (?, ?)", logTime, eventId);
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
        
        HashMap<String, AuditMetadata> metadata = saveMetadata();
        
        for (Map.Entry<String, AuditMetadata> m : metadata.entrySet()) {
            m.getValue().save();
        }
    }
    
    private void stripMetadata() throws SQLException {
        GoldenApple.getInstanceDatabaseManager().execute("DELETE FROM AuditLogParams WHERE AuditID=?", auditId);
    }
    
    protected AuditMetadata createMetadata(String name, Long iValue) {
        return createMetadata(name, iValue, null);
    }
    
    protected AuditMetadata createMetadata(String name, String sValue) {
        return createMetadata(name, null, sValue);
    }
    
    protected AuditMetadata createMetadata(String name, Long iValue, String sValue) {
        return new AuditMetadata(auditId, name, iValue, sValue);
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
    
    public enum AuditEventLevel {
        INFO("[i]", 0), WARNING("[!]", 1), SEVERE("[!!]", 2), EXTREME("[!!!]", 3);
        
        public String prefix;
        public int id;
        
        AuditEventLevel(String prefix, int id) {
            this.prefix = prefix;
            this.id = id;
        }
        
        public static AuditEventLevel fromId(int id) {
            for (AuditEventLevel level : AuditEventLevel.values()) {
                if (level.id == id) {
                    return level;
                }
            }
            return AuditEventLevel.EXTREME;
        }
    }
}
