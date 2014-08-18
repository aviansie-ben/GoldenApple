package com.bendude56.goldenapple.lock.audit;

import java.util.HashMap;

import com.bendude56.goldenapple.lock.LockedBlock.GuestLevel;
import com.bendude56.goldenapple.permissions.IPermissionUser;

public class LockOverrideEnableEvent extends LockEvent {
    public String level;
    
    public LockOverrideEnableEvent() {
        super(200, AuditEventLevel.INFO);
    }
    
    public LockOverrideEnableEvent(IPermissionUser user, GuestLevel level) {
        super(200, AuditEventLevel.INFO, user);
        
        this.level = level.toString();
    }
    
    @Override
    protected void loadMetadata(HashMap<String, AuditMetadata> metadata) {
        super.loadMetadata(metadata);
        
        level = metadata.get("level").valueString;
    }
    
    @Override
    protected HashMap<String, AuditMetadata> saveMetadata() {
        HashMap<String, AuditMetadata> metadata = super.saveMetadata();
        
        metadata.put("level", createMetadata("level", level));
        
        return metadata;
    }
    
    @Override
    public String formatMessage() {
        return user + " is now overriding locks at level " + level;
    }
}
