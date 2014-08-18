package com.bendude56.goldenapple.lock.audit;

import java.util.HashMap;

import com.bendude56.goldenapple.permissions.IPermissionUser;

public class LockDeleteEvent extends LockEvent {
    public long lock;
    
    public LockDeleteEvent() {
        super(204, AuditEventLevel.INFO);
    }
    
    public LockDeleteEvent(IPermissionUser user, long lock) {
        super(204, AuditEventLevel.INFO, user);
        
        this.lock = lock;
    }
    
    @Override
    protected void loadMetadata(HashMap<String, AuditMetadata> metadata) {
        super.loadMetadata(metadata);
        
        lock = metadata.get("lock").valueInt;
    }
    
    @Override
    protected HashMap<String, AuditMetadata> saveMetadata() {
        HashMap<String, AuditMetadata> metadata = super.saveMetadata();
        
        metadata.put("lock", createMetadata("lock", lock));
        
        return metadata;
    }
    
    @Override
    public String formatMessage() {
        return user + " has deleted lock " + lock;
    }
}
