package com.bendude56.goldenapple.permissions.audit;

import java.util.Map;

import com.bendude56.goldenapple.permissions.IPermissionObject;

public class PermissionRevokeEntry extends PermissionEntry {
    public String permission;
    
    public PermissionRevokeEntry() {
        super(401, AuditEntryLevel.SEVERE);
    }
    
    public PermissionRevokeEntry(String authorizingUser, IPermissionObject target, String permission) {
        super(401, AuditEntryLevel.SEVERE, authorizingUser, target);
        
        this.permission = permission;
    }
    
    @Override
    public String formatMessage() {
        return ((targetType == TargetType.GROUP) ? "Group " : "User ") + targetName + " (ID: " + targetId + ") has had permission " + permission + " revoked by " + authorizingUser;
    }
    
    @Override
    protected void loadMetadata(Map<String, AuditMetadata> metadata) {
        super.loadMetadata(metadata);
        
        permission = metadata.get("permission").valueString;
    }
    
    @Override
    protected Map<String, AuditMetadata> saveMetadata() {
        Map<String, AuditMetadata> metadata = super.saveMetadata();
        
        appendMetadata(metadata, "permission", permission);
        
        return metadata;
    }
    
}
