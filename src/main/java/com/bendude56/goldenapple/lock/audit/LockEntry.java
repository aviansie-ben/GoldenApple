package com.bendude56.goldenapple.lock.audit;

import java.util.HashMap;
import java.util.Map;

import com.bendude56.goldenapple.audit.AuditEntry;
import com.bendude56.goldenapple.permissions.IPermissionUser;

public abstract class LockEntry extends AuditEntry {
    public String user;
    
    public LockEntry(int entryId, AuditEntryLevel severity) {
        super(entryId, severity, "Lock");
    }
    
    public LockEntry(int entryId, AuditEntryLevel severity, IPermissionUser user) {
        this(entryId, severity);
        
        this.user = user.getLogName();
    }
    
    @Override
    protected void loadMetadata(Map<String, AuditMetadata> metadata) {
        this.user = metadata.get("user").valueString;
    }
    
    @Override
    protected Map<String, AuditMetadata> saveMetadata() {
        Map<String, AuditMetadata> metadata = new HashMap<String, AuditMetadata>();
        
        appendMetadata(metadata, "user", user);
        
        return metadata;
    }
    
}
