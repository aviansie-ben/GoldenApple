package com.bendude56.goldenapple.lock.audit;

import java.util.HashMap;

import com.bendude56.goldenapple.lock.LockedBlock.GuestLevel;
import com.bendude56.goldenapple.permissions.IPermissionUser;

public class LockOverrideEvent extends LockEvent {
    public String level;
    public long lock;
    
    public LockOverrideEvent() {
        super(202, AuditEventLevel.INFO);
    }
    
    public LockOverrideEvent(IPermissionUser user, GuestLevel level, long lock) {
        super(202, AuditEventLevel.INFO, user);
        
        this.level = level.toString();
        this.lock = lock;
    }
    
    @Override
    protected void loadMetadata(HashMap<String, AuditMetadata> metadata) {
        super.loadMetadata(metadata);
        
        level = metadata.get("level").valueString;
        lock = metadata.get("lock").valueInt;
    }
    
    @Override
    protected HashMap<String, AuditMetadata> saveMetadata() {
        HashMap<String, AuditMetadata> metadata = super.saveMetadata();
        
        metadata.put("level", createMetadata("level", level));
        metadata.put("lock", createMetadata("lock", lock));
        
        return metadata;
    }
    
    @Override
    public String formatMessage() {
        return user + " has overridden lock " + lock + " at level " + level;
    }
}
