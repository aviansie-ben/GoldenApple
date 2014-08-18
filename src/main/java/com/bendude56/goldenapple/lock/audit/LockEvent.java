package com.bendude56.goldenapple.lock.audit;

import java.util.HashMap;

import com.bendude56.goldenapple.audit.AuditEvent;
import com.bendude56.goldenapple.permissions.IPermissionUser;

public abstract class LockEvent extends AuditEvent {
    public String user;
    
    public LockEvent(int eventId, AuditEventLevel severity) {
        super(eventId, severity, "Lock");
    }
    
    public LockEvent(int eventId, AuditEventLevel severity, IPermissionUser user) {
        this(eventId, severity);
        
        this.user = user.getName();
    }
    
    @Override
    protected void loadMetadata(HashMap<String, AuditMetadata> metadata) {
        this.user = metadata.get("user").valueString;
    }
    
    @Override
    protected HashMap<String, AuditMetadata> saveMetadata() {
        HashMap<String, AuditMetadata> metadata = new HashMap<String, AuditMetadata>();
        
        metadata.put("user", createMetadata("user", user));
        
        return metadata;
    }
    
}
