package com.bendude56.goldenapple.permissions.audit;

import java.util.Map;

import com.bendude56.goldenapple.permissions.IPermissionGroup;
import com.bendude56.goldenapple.permissions.IPermissionObject;

public class GroupRemoveMemberEntry extends PermissionEntry {
    public String groupName;
    public long groupId;
    
    public GroupRemoveMemberEntry() {
        super(403, AuditEntryLevel.SEVERE);
    }
    
    public GroupRemoveMemberEntry(String authorizingUser, IPermissionObject target, IPermissionGroup group) {
        super(403, AuditEntryLevel.SEVERE, authorizingUser, target);
        
        this.groupName = group.getName();
        this.groupId = group.getId();
    }
    
    @Override
    public String formatMessage() {
        return ((targetType == TargetType.GROUP) ? "Group " : "User ") + targetName + " (ID: " + targetId + ") has been removed from group " + groupName + " (ID: " + groupId + ") by " + authorizingUser;
    }
    
    @Override
    protected void loadMetadata(Map<String, AuditMetadata> metadata) {
        super.loadMetadata(metadata);
        
        groupName = metadata.get("groupName").valueString;
        groupId = metadata.get("groupId").valueInt;
    }
    
    @Override
    protected Map<String, AuditMetadata> saveMetadata() {
        Map<String, AuditMetadata> metadata = super.saveMetadata();
        
        appendMetadata(metadata, "groupName", groupName);
        appendMetadata(metadata, "groupId", groupId);
        
        return metadata;
    }
    
}
