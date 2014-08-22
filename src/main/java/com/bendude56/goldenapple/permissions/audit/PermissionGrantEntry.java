package com.bendude56.goldenapple.permissions.audit;

import java.util.Map;

import com.bendude56.goldenapple.permissions.IPermissionObject;

public class PermissionGrantEntry extends PermissionEntry {
    public String permission;
    
    public PermissionGrantEntry() {
        super(400, AuditEntryLevel.SEVERE);
    }
    
    public PermissionGrantEntry(String authorizingUser, IPermissionObject target, String permission) {
        super(400, AuditEntryLevel.SEVERE, authorizingUser, target);
        
        this.permission = permission;
    }
    
    @Override
    public String formatMessage() {
        return ((targetType == TargetType.GROUP) ? "Group " : "User ") + targetName + " (ID: " + targetId + ") has been granted permission " + permission + " by " + authorizingUser;
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
