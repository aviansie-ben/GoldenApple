package com.bendude56.goldenapple.lock.audit;

import com.bendude56.goldenapple.permissions.IPermissionUser;

public class LockOverrideDisableEvent extends LockEvent {
    public LockOverrideDisableEvent() {
        super(201, AuditEventLevel.INFO);
    }
    
    public LockOverrideDisableEvent(IPermissionUser user) {
        super(201, AuditEventLevel.INFO, user);
    }
    
    @Override
    public String formatMessage() {
        return user + " is no longer overriding locks";
    }
}
