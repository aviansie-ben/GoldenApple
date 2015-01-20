package com.bendude56.goldenapple.permissions.audit;

import java.util.HashMap;
import java.util.Map;

import com.bendude56.goldenapple.audit.AuditEntry;
import com.bendude56.goldenapple.permissions.IPermissionGroup;
import com.bendude56.goldenapple.permissions.IPermissionObject;
import com.bendude56.goldenapple.permissions.IPermissionUser;

public abstract class PermissionEntry extends AuditEntry {
    
    public String authorizingUser;
    public TargetType targetType;
    public String targetName;
    public long targetId;
    
    public PermissionEntry(int entryId, AuditEntryLevel severity) {
        super(entryId, severity, "Permissions");
    }
    
    public PermissionEntry(int entryId, AuditEntryLevel severity, String authorizingUser, IPermissionObject target) {
        this(entryId, severity);
        
        this.authorizingUser = authorizingUser;
        
        if (target instanceof IPermissionUser) {
            this.targetType = TargetType.USER;
            this.targetName = ((IPermissionUser) target).getName();
            this.targetId = target.getId();
        } else if (target instanceof IPermissionGroup) {
            this.targetType = TargetType.GROUP;
            this.targetName = ((IPermissionGroup) target).getName();
            this.targetId = target.getId();
        } else {
            throw new UnsupportedOperationException();
        }
    }
    
    @Override
    protected void loadMetadata(Map<String, AuditMetadata> metadata) {
        authorizingUser = metadata.get("authorizingUser").valueString;
        targetType = TargetType.fromId(metadata.get("targetType").valueInt);
        targetName = metadata.get("targetName").valueString;
        targetId = metadata.get("targetId").valueInt;
    }
    
    @Override
    protected Map<String, AuditMetadata> saveMetadata() {
        Map<String, AuditMetadata> metadata = new HashMap<String, AuditMetadata>();
        
        appendMetadata(metadata, "authorizingUser", authorizingUser);
        appendMetadata(metadata, "targetType", targetType.getId());
        appendMetadata(metadata, "targetName", targetName);
        appendMetadata(metadata, "targetId", targetId);
        
        return metadata;
    }
    
    public enum TargetType {
        USER(0), GROUP(1);
        
        private final long id;
        
        private TargetType(long id) {
            this.id = id;
        }
        
        public long getId() {
            return id;
        }
        
        public static TargetType fromId(long id) {
            return (id == 0) ? USER : GROUP;
        }
    }
    
}
