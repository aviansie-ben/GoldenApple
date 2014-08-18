package com.bendude56.goldenapple.permissions.audit;

import com.bendude56.goldenapple.permissions.IPermissionObject;

public class ObjectCreateEvent extends PermissionEvent {
    public ObjectCreateEvent() {
        super(404, AuditEventLevel.INFO);
    }
    
    public ObjectCreateEvent(String authorizingUser, IPermissionObject target) {
        super(404, AuditEventLevel.INFO, authorizingUser, target);
    }
    
    @Override
    public String formatMessage() {
        return ((targetType == TargetType.GROUP) ? "Group " : "User ") + targetName + " (ID: " + targetId + ") has been created by " + authorizingUser;
    }
    
}
