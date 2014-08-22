package com.bendude56.goldenapple.punish.audit;

import java.util.HashMap;
import java.util.Map;

import com.bendude56.goldenapple.audit.AuditEntry;

public abstract class PunishEntry extends AuditEntry {
    
    public String authorizingUser;
    public String target;
    
    public PunishEntry(int entryId, AuditEntryLevel severity) {
        super(entryId, severity, "Punish");
    }
    
    public PunishEntry(int entryId, AuditEntryLevel severity, String authorizingUser, String target) {
        this(entryId, severity);
        
        this.authorizingUser = authorizingUser;
        this.target = target;
    }
    
    @Override
    protected void loadMetadata(Map<String, AuditMetadata> metadata) {
        authorizingUser = metadata.get("authorizingUser").valueString;
        target = metadata.get("target").valueString;
    }
    
    @Override
    protected Map<String, AuditMetadata> saveMetadata() {
        Map<String, AuditMetadata> metadata = new HashMap<String, AuditMetadata>();
        
        appendMetadata(metadata, "authorizingUser", authorizingUser);
        appendMetadata(metadata, "target", target);
        
        return metadata;
    }
    
}
