package com.bendude56.goldenapple.permissions.audit;

import java.util.HashMap;

import com.bendude56.goldenapple.permissions.IPermissionObject;

public class PermissionGrantEvent extends PermissionEvent {
    public String permission;
    
    public PermissionGrantEvent() {
        super(400, AuditEventLevel.SEVERE);
    }
    
    public PermissionGrantEvent(String authorizingUser, IPermissionObject target, String permission) {
        super(400, AuditEventLevel.SEVERE, authorizingUser, target);
        
        this.permission = permission;
    }
    
    @Override
    public String formatMessage() {
        return ((targetType == TargetType.GROUP) ? "Group " : "User ") + targetName + " (ID: " + targetId + ") has been granted permission " + permission + " by " + authorizingUser;
    }
    
    @Override
    protected void loadMetadata(HashMap<String, AuditMetadata> metadata) {
        super.loadMetadata(metadata);
        
        permission = metadata.get("permission").valueString;
    }
    
    @Override
    protected HashMap<String, AuditMetadata> saveMetadata() {
        HashMap<String, AuditMetadata> metadata = super.saveMetadata();
        
        metadata.put("permission", createMetadata("permission", permission));
        
        return metadata;
    }
    
}
