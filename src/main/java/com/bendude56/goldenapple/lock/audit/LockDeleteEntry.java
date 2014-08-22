package com.bendude56.goldenapple.lock.audit;

import java.util.Map;

import com.bendude56.goldenapple.permissions.IPermissionUser;

public class LockDeleteEntry extends LockEntry {
    public long lock;
    
    public LockDeleteEntry() {
        super(204, AuditEntryLevel.INFO);
    }
    
    public LockDeleteEntry(IPermissionUser user, long lock) {
        super(204, AuditEntryLevel.INFO, user);
        
        this.lock = lock;
    }
    
    @Override
    protected void loadMetadata(Map<String, AuditMetadata> metadata) {
        super.loadMetadata(metadata);
        
        lock = metadata.get("lock").valueInt;
    }
    
    @Override
    protected Map<String, AuditMetadata> saveMetadata() {
        Map<String, AuditMetadata> metadata = super.saveMetadata();
        
        appendMetadata(metadata, "lock", lock);
        
        return metadata;
    }
    
    @Override
    public String formatMessage() {
        return user + " has deleted lock " + lock;
    }
}
