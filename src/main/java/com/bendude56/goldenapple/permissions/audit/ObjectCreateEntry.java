package com.bendude56.goldenapple.permissions.audit;

import com.bendude56.goldenapple.permissions.IPermissionObject;

public class ObjectCreateEntry extends PermissionEntry {
    public ObjectCreateEntry() {
        super(404, AuditEntryLevel.INFO);
    }
    
    public ObjectCreateEntry(String authorizingUser, IPermissionObject target) {
        super(404, AuditEntryLevel.INFO, authorizingUser, target);
    }
    
    @Override
    public String formatMessage() {
        return ((targetType == TargetType.GROUP) ? "Group " : "User ") + targetName + " (ID: " + targetId + ") has been created by " + authorizingUser;
    }
    
}
