package com.bendude56.goldenapple.permissions.audit;

import com.bendude56.goldenapple.permissions.IPermissionObject;

public class ObjectDeleteEvent extends PermissionEvent {
    
    public ObjectDeleteEvent(String authorizingUser, IPermissionObject target) {
        super(405, AuditEventLevel.SEVERE, authorizingUser, target);
    }
    
    @Override
    public String formatMessage() {
        return ((targetType == TargetType.GROUP) ? "Group " : "User ") + targetName + " (ID: " + targetId + ") has been deleted by " + authorizingUser;
    }
    
}
