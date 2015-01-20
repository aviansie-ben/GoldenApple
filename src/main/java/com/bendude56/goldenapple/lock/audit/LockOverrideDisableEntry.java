package com.bendude56.goldenapple.lock.audit;

import com.bendude56.goldenapple.permissions.IPermissionUser;

public class LockOverrideDisableEntry extends LockEntry {
    public LockOverrideDisableEntry() {
        super(201, AuditEntryLevel.INFO);
    }
    
    public LockOverrideDisableEntry(IPermissionUser user) {
        super(201, AuditEntryLevel.INFO, user);
    }
    
    @Override
    public String formatMessage() {
        return user + " is no longer overriding locks";
    }
}
