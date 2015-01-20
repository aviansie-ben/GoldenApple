package com.bendude56.goldenapple.permissions.audit;

import com.bendude56.goldenapple.permissions.IPermissionObject;

public class ObjectDeleteEntry extends PermissionEntry {
    public ObjectDeleteEntry() {
        super(405, AuditEntryLevel.SEVERE);
    }
    
    public ObjectDeleteEntry(String authorizingUser, IPermissionObject target) {
        super(405, AuditEntryLevel.SEVERE, authorizingUser, target);
    }
    
    @Override
    public String formatMessage() {
        return ((targetType == TargetType.GROUP) ? "Group " : "User ") + targetName + " (ID: " + targetId + ") has been deleted by " + authorizingUser;
    }
    
}
