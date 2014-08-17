package com.bendude56.goldenapple.permissions.audit;

import java.util.HashMap;

import com.bendude56.goldenapple.permissions.IPermissionObject;

public class PermissionRevokeEvent extends PermissionEvent {
    public String permission;
    
    public PermissionRevokeEvent(String authorizingUser, IPermissionObject target, String permission) {
        super(401, AuditEventLevel.SEVERE, authorizingUser, target);
        
        this.permission = permission;
    }
    
    @Override
    public String formatMessage() {
        return ((targetType == TargetType.GROUP) ? "Group " : "User ") + targetName + " (ID: " + targetId + ") has had permission " + permission + " revoked by " + authorizingUser;
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
