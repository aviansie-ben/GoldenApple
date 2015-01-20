package com.bendude56.goldenapple.lock.audit;

import java.util.Map;

import com.bendude56.goldenapple.lock.LockedBlock.GuestLevel;
import com.bendude56.goldenapple.permissions.IPermissionUser;

public class LockOverrideEntry extends LockEntry {
    public String level;
    public long lock;
    
    public LockOverrideEntry() {
        super(202, AuditEntryLevel.INFO);
    }
    
    public LockOverrideEntry(IPermissionUser user, GuestLevel level, long lock) {
        super(202, AuditEntryLevel.INFO, user);
        
        this.level = level.toString();
        this.lock = lock;
    }
    
    @Override
    protected void loadMetadata(Map<String, AuditMetadata> metadata) {
        super.loadMetadata(metadata);
        
        level = metadata.get("level").valueString;
        lock = metadata.get("lock").valueInt;
    }
    
    @Override
    protected Map<String, AuditMetadata> saveMetadata() {
        Map<String, AuditMetadata> metadata = super.saveMetadata();
        
        appendMetadata(metadata, "level", level);
        appendMetadata(metadata, "lock", lock);
        
        return metadata;
    }
    
    @Override
    public String formatMessage() {
        return user + " has overridden lock " + lock + " at level " + level;
    }
}
