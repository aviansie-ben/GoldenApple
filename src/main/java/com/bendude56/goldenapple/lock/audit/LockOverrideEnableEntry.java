package com.bendude56.goldenapple.lock.audit;

import java.util.Map;

import com.bendude56.goldenapple.lock.LockedBlock.GuestLevel;
import com.bendude56.goldenapple.permissions.IPermissionUser;

public class LockOverrideEnableEntry extends LockEntry {
    public String level;
    
    public LockOverrideEnableEntry() {
        super(200, AuditEntryLevel.INFO);
    }
    
    public LockOverrideEnableEntry(IPermissionUser user, GuestLevel level) {
        super(200, AuditEntryLevel.INFO, user);
        
        this.level = level.toString();
    }
    
    @Override
    protected void loadMetadata(Map<String, AuditMetadata> metadata) {
        super.loadMetadata(metadata);
        
        level = metadata.get("level").valueString;
    }
    
    @Override
    protected Map<String, AuditMetadata> saveMetadata() {
        Map<String, AuditMetadata> metadata = super.saveMetadata();
        
        appendMetadata(metadata, "level", level);
        
        return metadata;
    }
    
    @Override
    public String formatMessage() {
        return user + " is now overriding locks at level " + level;
    }
}
